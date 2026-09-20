package lms.core;

/**
 * Thrown by an Add/Edit form when the user tries to save with a
 * required field left blank. The message is written to be shown
 * directly to the user (e.g. in a JOptionPane), not just logged.
 */
@SuppressWarnings("serial")
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
