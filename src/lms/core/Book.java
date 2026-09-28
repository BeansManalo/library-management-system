package lms.core;

/**
 * A single book in the library's catalog. ISBN is its identifier --
 * there's no separate internal Book ID, since the ISBN already
 * uniquely names the title and staff already have it on hand.
 * Plain data holder (POJO) -- no logic beyond getters/setters, so it
 * stays easy to swap for a database-backed row later.
 */
public class Book {

    private String title;
    private String author;
    private String genre;
    private String isbn;
    private String publisher;
    private String publicationDate;
    private int totalCopies;
    private int availableCopies;
    // Free-form, comma-separated search keywords -- separate from Genre,
    // which is a single category; a book can carry several tags.
    private String tags;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    /** True as long as at least one copy isn't checked out. */
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    /** True if any searchable field (including the tags) contains the given lowercase text. */
    public boolean matches(String query) {
        return contains(title, query) || contains(author, query) || contains(genre, query)
            || contains(isbn, query) || contains(publisher, query) || contains(tags, query);
    }

    private static boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }
}
