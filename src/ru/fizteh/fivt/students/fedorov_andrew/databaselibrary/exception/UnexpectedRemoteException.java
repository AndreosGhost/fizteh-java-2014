package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception;

import java.rmi.RemoteException;

public class UnexpectedRemoteException extends RuntimeException {
    public UnexpectedRemoteException(RemoteException exc) {
        super(exc.getMessage(), exc.getCause());
    }
}
