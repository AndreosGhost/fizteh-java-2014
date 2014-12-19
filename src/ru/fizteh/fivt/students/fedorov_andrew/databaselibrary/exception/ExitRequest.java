package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception;

/**
 * This exception describes exit request. Used in interpreter.
 */
public class ExitRequest extends RuntimeException {
    public static final int NORMAL_TERMINATION_CODE = 0;

    private final int code;

    public ExitRequest(int code) {
        super("Exit request with status = " + code);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
