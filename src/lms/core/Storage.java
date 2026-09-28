package lms.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Permanent storage for the Library.
 *
 * Location: %LOCALAPPDATA%\LeMon.S (hidden), holding library.lms plus the
 * five newest autosaves. Every save writes the same bytes to library.lms
 * and to a new autosave named after the time of the save
 * (autosave_yyyyMMdd_HHmmss.lms); older autosaves beyond five are deleted.
 * If library.lms is damaged or tampered with, the newest autosave has the
 * latest state and the older ones are further fallbacks.
 *
 * Separately, at most once a day a save also leaves a copy in
 * Documents\LeMon.S Backups (library_yyyyMMdd_HHmmss.lms, newest 14 kept).
 * Load Library and Delete Library never touch that folder, so it's the last
 * resort if everything above gets overwritten. Any backup opens with Load Library.
 *
 * File layout: "LMS1" | 12-byte IV | AES-256-GCM ciphertext | SHA-256 of
 * everything before it. The payload is the library split into tables in
 * third normal form (a row's id is its position in its table):
 *
 *   author(name)   genre(name)   publisher(name)   tag(name)
 *   book(isbn, title, author, genre, publisher, publication_date,
 *        total_copies, available_copies, in_catalog)
 *   book_tag(book, tag)
 *   member(member_id, first_name, last_name, contact_number, email,
 *          address, birth_date, join_date, end_date)
 *   loan(member, book, quantity, borrow_date, due_date, return_date)
 *
 * in_catalog is false for a book that was deleted from the catalog but is
 * still named by a returned loan in some member's history.
 */
public final class Storage {

    private static final int AUTOSAVES = 5;
    private static final int BACKUPS_KEPT = 14;
    private static final Duration BACKUP_EVERY = Duration.ofDays(1);
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final byte[] MAGIC = {'L', 'M', 'S', '1'};
    private static final int IV_LENGTH = 12;
    private static final int SUM_LENGTH = 32;
    private static final Path DIR = Path.of(
        System.getenv("LOCALAPPDATA") != null ? System.getenv("LOCALAPPDATA") : System.getProperty("user.home"),
        "LeMon.S");
    private static final Path MAIN = DIR.resolve("library.lms");
    private static final Path BACKUP_DIR = Path.of(System.getProperty("user.home"), "Documents", "LeMon.S Backups");
    private static final SecureRandom RANDOM = new SecureRandom();

    // The key is built into the app, so exported files open on any PC running it.
    private static final SecretKeySpec KEY;
    static {
        try {
            byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(
                "LeMon.S library".toCharArray(), "LeMon.S".getBytes(StandardCharsets.UTF_8), 100_000, 256)).getEncoded();
            KEY = new SecretKeySpec(key, "AES");
        } catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Storage() {
    }

    /** Saves to library.lms and a new timestamped autosave, then trims the autosaves. Call after every change. */
    public static void save(Library library) throws IOException {
        byte[] data = seal(encode(library));
        ensureDir();
        write(DIR.resolve("autosave_" + STAMP.format(LocalDateTime.now()) + ".lms"), data);
        write(MAIN, data);
        prune(newestFirst(DIR, "autosave"), AUTOSAVES);
        backupIfDue(data);
    }

    /** At most once a day, leaves a copy in the Documents backup folder. Best effort: it never fails a save. */
    private static void backupIfDue(byte[] data) {
        try {
            List<Path> old = newestFirst(BACKUP_DIR, "library_");
            if (!old.isEmpty() && Files.getLastModifiedTime(old.get(0)).toInstant().plus(BACKUP_EVERY).isAfter(Instant.now())) {
                return;
            }
            Files.createDirectories(BACKUP_DIR);
            write(BACKUP_DIR.resolve("library_" + STAMP.format(LocalDateTime.now()) + ".lms"), data);
            prune(newestFirst(BACKUP_DIR, "library_"), BACKUPS_KEPT);
        } catch (IOException e) {
            // the real save already succeeded; a missed backup shouldn't look like a failed save
        }
    }

    /**
     * Loads the library from library.lms, or from the newest autosave that
     * checks out if that fails. Returns null if nothing has ever been saved;
     * throws if saves exist but none of them can be loaded.
     */
    public static Library load() throws ValidationException {
        List<Path> files = new ArrayList<>();
        files.add(MAIN);
        try {
            files.addAll(newestFirst(DIR, "autosave"));
        } catch (IOException e) {
            throw new ValidationException("The library failed to load.");
        }
        boolean found = false;
        for (Path file : files) {
            if (Files.exists(file)) {
                found = true;
                try {
                    return open(Files.readAllBytes(file));
                } catch (IOException | ValidationException e) {
                    // fall through to the next autosave
                }
            }
        }
        if (found) {
            throw new ValidationException("The library failed to load.");
        }
        return null;
    }

    /** Writes a standalone copy of the library to {@code file}, in the same format as library.lms. */
    public static void export(Library library, Path file) throws IOException {
        write(file, seal(encode(library)));
    }

    /**
     * Checks {@code file} (format and checksum). If it's a proper library
     * file, it replaces library.lms, the autosaves are deleted, and the
     * library it holds is returned. Otherwise nothing changes.
     */
    public static Library importFile(Path file) throws IOException, ValidationException {
        byte[] data = Files.readAllBytes(file);
        Library library = open(data);
        ensureDir();
        write(MAIN, data);
        deleteAutosaves();
        return library;
    }

    /** Deletes library.lms and every autosave. */
    public static void delete() throws IOException {
        Files.deleteIfExists(MAIN);
        deleteAutosaves();
    }

    /** The .lms files in {@code dir} starting with {@code prefix}, newest first (their names sort by time). */
    private static List<Path> newestFirst(Path dir, String prefix) throws IOException {
        if (Files.notExists(dir)) {
            return List.of();
        }
        try (Stream<Path> all = Files.list(dir)) {
            return all.filter(p -> p.getFileName().toString().startsWith(prefix) && p.toString().endsWith(".lms"))
                .sorted(Comparator.reverseOrder()).toList();
        }
    }

    /** Deletes everything in a newest-first list except the first {@code keep} files. */
    private static void prune(List<Path> newestFirst, int keep) throws IOException {
        for (Path old : newestFirst.subList(Math.min(keep, newestFirst.size()), newestFirst.size())) {
            Files.delete(old);
        }
    }

    private static void deleteAutosaves() throws IOException {
        prune(newestFirst(DIR, "autosave"), 0);
    }

    private static void ensureDir() throws IOException {
        if (Files.notExists(DIR)) {
            Files.createDirectories(DIR);
            try {
                Files.setAttribute(DIR, "dos:hidden", true); // Windows only
            } catch (UnsupportedOperationException | IOException e) {
                // not on Windows -- the folder just stays visible
            }
        }
    }

    private static void write(Path file, byte[] data) throws IOException {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(temp, data);
        Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE);
    }

    // ---- encryption + checksum ----

    private static byte[] seal(byte[] plain) throws IOException {
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(128, iv));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(MAGIC);
            out.write(iv);
            out.write(cipher.doFinal(plain));
            out.write(MessageDigest.getInstance("SHA-256").digest(out.toByteArray()));
            return out.toByteArray();
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    private static Library open(byte[] file) throws ValidationException {
        int end = file.length - SUM_LENGTH;
        if (end < MAGIC.length + IV_LENGTH + 16 || !Arrays.equals(Arrays.copyOf(file, MAGIC.length), MAGIC)) {
            throw new ValidationException("This isn't a LeMon.S library file.");
        }
        try {
            byte[] sum = MessageDigest.getInstance("SHA-256").digest(Arrays.copyOf(file, end));
            if (!MessageDigest.isEqual(sum, Arrays.copyOfRange(file, end, file.length))) {
                throw new ValidationException("The checksum doesn't match. The file is damaged or has been tampered with.");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, KEY, new GCMParameterSpec(128, file, MAGIC.length, IV_LENGTH));
            int start = MAGIC.length + IV_LENGTH;
            return decode(cipher.doFinal(file, start, end - start));
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("The library file is damaged and can't be read.");
        }
    }

    // ---- library <-> tables ----

    private static byte[] encode(Library library) throws IOException {
        Map<String, Integer> authors = new LinkedHashMap<>();
        Map<String, Integer> genres = new LinkedHashMap<>();
        Map<String, Integer> publishers = new LinkedHashMap<>();
        Map<String, Integer> tags = new LinkedHashMap<>();
        Map<Book, Integer> books = new LinkedHashMap<>();
        for (Book book : library.getBooks()) {
            books.put(book, books.size());
        }
        int inCatalog = books.size();
        int loanCount = 0;
        for (Member member : library.getMembers()) {
            for (Loan loan : member.getLoans()) {
                books.putIfAbsent(loan.getBook(), books.size());
                loanCount++;
            }
        }
        for (Book book : books.keySet()) {
            authors.putIfAbsent(text(book.getAuthor()), authors.size());
            genres.putIfAbsent(text(book.getGenre()), genres.size());
            publishers.putIfAbsent(text(book.getPublisher()), publishers.size());
            for (String tag : tagsOf(book)) {
                tags.putIfAbsent(tag, tags.size());
            }
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        for (Map<String, Integer> table : List.of(authors, genres, publishers, tags)) {
            out.writeInt(table.size());
            for (String name : table.keySet()) {
                out.writeUTF(name);
            }
        }

        out.writeInt(books.size());
        int row = 0;
        for (Book book : books.keySet()) {
            out.writeUTF(text(book.getIsbn()));
            out.writeUTF(text(book.getTitle()));
            out.writeInt(authors.get(text(book.getAuthor())));
            out.writeInt(genres.get(text(book.getGenre())));
            out.writeInt(publishers.get(text(book.getPublisher())));
            out.writeUTF(text(book.getPublicationDate()));
            out.writeInt(book.getTotalCopies());
            out.writeInt(book.getAvailableCopies());
            out.writeBoolean(row++ < inCatalog);
        }

        int bookTagCount = 0;
        for (Book book : books.keySet()) {
            bookTagCount += tagsOf(book).size();
        }
        out.writeInt(bookTagCount);
        for (Map.Entry<Book, Integer> book : books.entrySet()) {
            for (String tag : tagsOf(book.getKey())) {
                out.writeInt(book.getValue());
                out.writeInt(tags.get(tag));
            }
        }

        out.writeInt(library.getMembers().size());
        for (Member member : library.getMembers()) {
            out.writeUTF(text(member.getMemberId()));
            out.writeUTF(text(member.getFirstName()));
            out.writeUTF(text(member.getLastName()));
            out.writeUTF(text(member.getContactNumber()));
            out.writeUTF(text(member.getEmail()));
            out.writeUTF(text(member.getAddress()));
            out.writeUTF(text(member.getBirthDate()));
            out.writeUTF(text(member.getJoinDate()));
            out.writeUTF(text(member.getEndDate()));
        }

        out.writeInt(loanCount);
        int memberRow = 0;
        for (Member member : library.getMembers()) {
            for (Loan loan : member.getLoans()) {
                out.writeInt(memberRow);
                out.writeInt(books.get(loan.getBook()));
                out.writeInt(loan.getQuantity());
                out.writeLong(loan.getBorrowDate().toEpochDay());
                out.writeLong(loan.getDueDate().toEpochDay());
                out.writeLong(loan.getReturnDate() == null ? Long.MIN_VALUE : loan.getReturnDate().toEpochDay());
            }
            memberRow++;
        }
        return bytes.toByteArray();
    }

    private static Library decode(byte[] data) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        List<String> authors = names(in);
        List<String> genres = names(in);
        List<String> publishers = names(in);
        List<String> tags = names(in);

        Library library = new Library();
        List<Book> books = new ArrayList<>();
        for (int n = in.readInt(); n > 0; n--) {
            Book book = new Book();
            book.setIsbn(in.readUTF());
            book.setTitle(in.readUTF());
            book.setAuthor(authors.get(in.readInt()));
            book.setGenre(genres.get(in.readInt()));
            book.setPublisher(publishers.get(in.readInt()));
            book.setPublicationDate(in.readUTF());
            book.setTotalCopies(in.readInt());
            book.setAvailableCopies(in.readInt());
            book.setTags("");
            books.add(book);
            if (in.readBoolean()) {
                library.getBooks().add(book);
            }
        }
        for (int n = in.readInt(); n > 0; n--) {
            Book book = books.get(in.readInt());
            String tag = tags.get(in.readInt());
            book.setTags(book.getTags().isEmpty() ? tag : book.getTags() + ", " + tag);
        }

        for (int n = in.readInt(); n > 0; n--) {
            Member member = new Member();
            member.setMemberId(in.readUTF());
            member.setFirstName(in.readUTF());
            member.setLastName(in.readUTF());
            member.setContactNumber(in.readUTF());
            member.setEmail(in.readUTF());
            member.setAddress(in.readUTF());
            member.setBirthDate(in.readUTF());
            member.setJoinDate(in.readUTF());
            member.setEndDate(in.readUTF());
            library.getMembers().add(member);
        }
        for (int n = in.readInt(); n > 0; n--) {
            Member member = library.getMembers().get(in.readInt());
            Book book = books.get(in.readInt());
            Loan loan = new Loan(book, in.readInt(), LocalDate.ofEpochDay(in.readLong()), LocalDate.ofEpochDay(in.readLong()));
            long returned = in.readLong();
            if (returned != Long.MIN_VALUE) {
                loan.setReturnDate(LocalDate.ofEpochDay(returned));
            }
            member.addLoan(loan);
        }
        return library;
    }

    private static List<String> names(DataInputStream in) throws IOException {
        List<String> names = new ArrayList<>();
        for (int n = in.readInt(); n > 0; n--) {
            names.add(in.readUTF());
        }
        return names;
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    /** A book's comma-separated tags as a set of distinct, trimmed, non-empty names. */
    private static Set<String> tagsOf(Book book) {
        Set<String> tags = new LinkedHashSet<>();
        for (String tag : text(book.getTags()).split(",")) {
            if (!tag.trim().isEmpty()) {
                tags.add(tag.trim());
            }
        }
        return tags;
    }
}
