
package pt.uac.cafeteria.model;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Unchecked exception for errors that compromise the correct functioning of
 * the application. For example, if one object can't be persisted because of
 * an IOException, then there is data that won't be saved and it may leave the
 * application in an inconsistent state.
 * <p>
 * This exception should be caught soon in the call stack to signal a problem
 * that prevents the correct functioning of the application.
 */
public class ApplicationException extends RuntimeException {

    /**
     * Logs an exception.
     *
     * @param e original exception thrown.
     */
    public static void log(Exception e) {
        if (!(e instanceof ApplicationException)) {
            Logger.getLogger(e.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Creates a new instance of <code>ApplicationException</code>
     * without detail message.
     */
    public ApplicationException() {
    }

    /**
     * Constructs an instance of <code>ApplicationException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ApplicationException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>ApplicationException</code>
     * with the specified detail message and original exception thrown.
     * <p>
     * Used in re-throwing a lower level Exception (e.g., IOException,
     * SQLException) and logs it for debugging purposes, while giving the
     * user a more friendly message.
     *
     * @param msg the detail message.
     * @param e original exception thrown.
     */
    public ApplicationException(String msg, Exception e) {
        super(msg);
        log(e);
    }

    /**
     * Creates an instance of <code>ApplicationException</code>
     * based on another exception.
     * <p>
     * Used in re-throwing a lower level Exception (e.g., IOException,
     * SQLException) and logs it for debugging purposes.
     *
     * @param e the original exception thrown.
     */
    public ApplicationException(Exception e) {
        log(e);
    }
}
