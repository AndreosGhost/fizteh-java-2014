package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception;

public class ExecutionNotPermittedException extends Exception {
    public ExecutionNotPermittedException(String message,
                                          Throwable cause,
                                          boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ExecutionNotPermittedException() {
    }

    public ExecutionNotPermittedException(String message) {
        super(message);
    }

    public ExecutionNotPermittedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecutionNotPermittedException(Throwable cause) {
        super(cause);
    }
}
