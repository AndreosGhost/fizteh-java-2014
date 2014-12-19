package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception;

/**
 * This exception occurs when object can be no longer used because of {@link
 * ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController} control.
 */
public class InvalidatedObjectException extends IllegalStateException {
    public InvalidatedObjectException() {
    }

    public InvalidatedObjectException(String s) {
        super(s);
    }

    public InvalidatedObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidatedObjectException(Throwable cause) {
        super(cause);
    }
}
