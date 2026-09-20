package lms.core;

/**
 * A single book in the library's catalog.
 * Plain data holder (POJO) -- no logic beyond getters/setters, so it
 * stays easy to swap for a database-backed row later.
 */
public class Book {

    private String bookId;
    private String title;
    private String author;
    private String genre;
    private boolean available;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
