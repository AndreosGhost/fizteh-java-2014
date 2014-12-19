package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Stub of remote table that is transferred from server to client. <br/>
 * I created this stub because task API is not friendly and there are no RemoteExceptions.
 */
class RemoteTableStub implements Table, Serializable {
    private final IRemoteTable table;

    public RemoteTableStub(RemoteTableImpl table) {
        this.table = table;
    }

    @Override
    public Storeable put(String key, Storeable value) throws ColumnFormatException {
        try {
            return table.put(key, value);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public Storeable remove(String key) {
        try {
            return table.remove(key);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public int size() {
        try {
            return table.size();
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public List<String> list() {
        try {
            return table.list();
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public int commit() throws IOException {
        try {
            return table.commit();
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public int rollback() {
        try {
            return table.rollback();
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public int getNumberOfUncommittedChanges() {
        try {
            return table.getNumberOfUncommittedChanges();
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public int getColumnsCount() {
        try {
            return table.getColumnsCount();
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        try {
            return table.getColumnType(columnIndex);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public String getName() {
        try {
            return table.getName();
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public Storeable get(String key) {
        try {
            return table.get(key);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }
}
