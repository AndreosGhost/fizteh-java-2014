package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.InvalidatedObjectException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.UnexpectedRemoteException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController.KillLock;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController.UseLock;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Stub of remote table that is transferred from server to client. <br/>
 * I created this stub because task API is not friendly and there are no RemoteExceptions.
 */
final class RemoteTableStub implements Table, Serializable, Closeable {
    // Frankly speaking there is no need to perform forceClose() method. It it done to decrease count of
    // remote requests.

    /**
     * Table name is cached for correct close handling at client's local.
     */
    private final String tableName;

    private final IRemoteTable remoteTable;
    /**
     * For validity control on client side.
     */
    private final ValidityController validityController = new ValidityController();
    /**
     * Local variable at the client side.
     */
    private transient RemoteTableProviderStub providerStub;

    public RemoteTableStub(RemoteTableImpl remoteTable, String tableName) {
        this.remoteTable = remoteTable;
        this.tableName = tableName;
    }

    /**
     * Called at client side when we get the stub.
     */
    public void bindToProviderStub(RemoteTableProviderStub providerStub) {
        this.providerStub = providerStub;
    }

    @Override
    public void close() {
        try (KillLock lock = validityController.useAndKill()) {
            providerStub.onTableStubClosed(tableName);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    private void forceClose(UseLock activeUseLock) {
        try (KillLock killLock = activeUseLock.obtainKillLockInstead()) {
            close();
        }
    }

    @Override
    public Storeable put(String key, Storeable value) throws ColumnFormatException {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteTable.put(key, value);
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public Storeable remove(String key) {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteTable.remove(key);
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public int size() {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteTable.size();
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public List<String> list() {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteTable.list();
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public int commit() throws IOException {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteTable.commit();
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public int rollback() {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteTable.rollback();
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public int getNumberOfUncommittedChanges() {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteTable.getNumberOfUncommittedChanges();
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public int getColumnsCount() {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteTable.getColumnsCount();
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteTable.getColumnType(columnIndex);
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public String getName() {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteTable.getName();
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public Storeable get(String key) {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteTable.get(key);
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }
}
