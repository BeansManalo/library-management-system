package lms.core;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory home for every Book and Member the app knows about.
 *
 * MainFrame owns the single instance of this class and every panel
 * reaches it through mainFrame.getLibrary(), so there is exactly one
 * copy of the data no matter which screen is showing. When the
 * planned database module is added later, the two lists below are
 * the only thing that needs to change (e.g. load/save through a DAO
 * instead of keeping everything in memory) -- none of the GUI code
 * has to change.
 */
public class Library {

    private final List<Book> books = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();

    public List<Book> getBooks() {
        return books;
    }

    public List<Member> getMembers() {
        return members;
    }
}
