package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.InvalidatedObjectException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.UnexpectedRemoteException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController.KillLock;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController.UseLock;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Stub of remote table provider that is exploited on the client side.
 */
final class RemoteTableProviderStub implements RemoteTableProvider, Serializable {
    /**
     * Wrapped remote table provider.
     */
    private final IRemoteTableProvider remoteProvider;
    /**
     * Read-write sync on tableStubs. Because getTable() is very popular :)
     */
    private final ReadWriteLock tableStubsLock = new ReentrantReadWriteLock(true);
    /**
     * For synchronization on client side.
     */
    private transient ValidityController validityController;
    /**
     * Local stubs associated with this provider.
     */
    private transient Map<String, RemoteTableStub> tableStubs;

    private transient boolean tableStubClosedByMe;

    /**
     * Local storage stub that manages instances of {@link ru.fizteh.fivt.students.fedorov_andrew
     * .databaselibrary.db.remote.RemoteTableProviderStub}.
     */
    private transient RemoteDatabaseStorage storage;

    /**
     * Location on storage that identifies this provider stub.
     */
    private transient String locationInStorage;

    public RemoteTableProviderStub(IRemoteTableProvider remoteProvider) {
        this.remoteProvider = remoteProvider;
    }

    public void bindToStorage(RemoteDatabaseStorage storage, String locationInStorage) {
        this.storage = storage;
        this.locationInStorage = locationInStorage;

        // Initializing transient fields.
        validityController = new ValidityController();
        tableStubs = new HashMap<>();
        tableStubClosedByMe = false;
    }

    private RemoteTableStub tryReplaceTableStub(String name, RemoteTableStub tableStub) {
        if (tableStub == null) {
            return null;
        }

        Lock lock = tableStubsLock.readLock();
        lock.lock();
        try {
            RemoteTableStub oldStub = tableStubs.get(name);
            if (oldStub == null) {
                lock.unlock();
                lock = tableStubsLock.writeLock();
                lock.lock();
                oldStub = tableStubs.get(name);
                if (oldStub == null) {
                    tableStubs.put(name, tableStub);
                    tableStub.bindToProviderStub(this);
                    return tableStub;
                } else {
                    return oldStub;
                }
            } else {
                return oldStub;
            }
        } finally {
            lock.unlock();
        }
    }

    private void removeTableStub(String tableName) {
        tableStubsLock.writeLock().lock();
        try {
            RemoteTableStub tableStub = tableStubs.get(tableName);
            if (tableStub != null) {
                tableStub.close();
            }
        } finally {
            tableStubsLock.writeLock().unlock();
        }
    }

    public void onTableStubClosed(String tableName) {
        tableStubsLock.writeLock().lock();
        try {
            if (!tableStubClosedByMe) {
                tableStubs.remove(tableName);
            }
        } finally {
            tableStubsLock.writeLock().unlock();
        }
    }

    @Override
    public void close() {
        try (KillLock lock = validityController.useAndKill()) {
            tableStubsLock.writeLock().lock();
            try {
                tableStubClosedByMe = true;
                tableStubs.values().forEach(RemoteTableStub::close);
                storage.onProviderStubClosed(locationInStorage);
            } finally {
                tableStubsLock.writeLock().unlock();
            }
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
    public Table getTable(String name) {
        try (UseLock lock = validityController.use()) {
            try {
                return tryReplaceTableStub(name, remoteProvider.getTable(name));
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public Table createTable(String name, List<Class<?>> columnTypes) throws IOException {
        try (UseLock lock = validityController.use()) {
            try {
                return tryReplaceTableStub(name, remoteProvider.createTable(name, columnTypes));
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public void removeTable(String name) throws IOException {
        try (UseLock lock = validityController.use()) {
            try {
                removeTableStub(name);
                remoteProvider.removeTable(name);
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public Storeable deserialize(Table table, String value) throws ParseException {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteProvider.deserialize(table, value);
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteProvider.serialize(table, value);
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public Storeable createFor(Table table) {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteProvider.createFor(table);
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public Storeable createFor(Table table, List<?> values)
            throws ColumnFormatException, IndexOutOfBoundsException {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteProvider.createFor(table, values);
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public List<String> getTableNames() {
        try (UseLock lock = validityController.use()) {
            try {
                return remoteProvider.getTableNames();
            } catch (InvalidatedObjectException exc) {
                forceClose(lock);
                throw exc;
            }
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }
}
