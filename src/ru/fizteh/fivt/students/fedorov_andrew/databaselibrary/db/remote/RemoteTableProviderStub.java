package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.List;

/**
 * Stub of remote table provider that is transferred from server to client. <br/>
 * I created this stub because task API is not friendly and there are no RemoteExceptions.
 */
class RemoteTableProviderStub implements RemoteTableProvider, Serializable {
    private final IRemoteTableProvider provider;

    public RemoteTableProviderStub(IRemoteTableProvider provider) {
        this.provider = provider;
    }

    @Override
    public void close() throws IOException {
        // I can't influence rmi connections -> do nothing.
    }

    @Override
    public Table getTable(String name) {
        try {
            return provider.getTable(name);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public Table createTable(String name, List<Class<?>> columnTypes) throws IOException {
        try {
            return provider.createTable(name, columnTypes);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public void removeTable(String name) throws IOException {
        try {
            provider.removeTable(name);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public Storeable deserialize(Table table, String value) throws ParseException {
        try {
            return provider.deserialize(table, value);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        try {
            return provider.serialize(table, value);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public Storeable createFor(Table table) {
        try {
            return provider.createFor(table);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public Storeable createFor(Table table, List<?> values)
            throws ColumnFormatException, IndexOutOfBoundsException {
        try {
            return provider.createFor(table, values);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public List<String> getTableNames() {
        try {
            return provider.getTableNames();
        } catch (RemoteException exc) {
            throw new RuntimeException(exc);
        }
    }
}
