package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.util.List;

class RemoteTableProviderImpl extends UnicastRemoteObject implements IRemoteTableProvider {
    private final TableProvider provider;

    public RemoteTableProviderImpl(TableProvider provider) throws RemoteException {
        this.provider = provider;
    }

    private Table wrapIntoRemote(Table table) throws RemoteException {
        return table == null ? null : new RemoteTableStub(new RemoteTableImpl(table));
    }

    @Override
    public Table getTable(String name) {
        try {
            Table table = provider.getTable(name);
            return wrapIntoRemote(table);
        } catch (RemoteException exc) {
            throw new RuntimeException(exc.getMessage(), exc.getCause());
        }
    }

    @Override
    public Table createTable(String name, List<Class<?>> columnTypes) throws IOException {
        Table table = provider.createTable(name, columnTypes);
        return wrapIntoRemote(table);
    }

    @Override
    public void removeTable(String name) throws IOException {
        provider.removeTable(name);
    }

    @Override
    public Storeable deserialize(Table table, String value) throws ParseException {
        return provider.deserialize(table, value);
    }

    @Override
    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        return provider.serialize(table, value);
    }

    @Override
    public Storeable createFor(Table table) {
        return provider.createFor(table);
    }

    @Override
    public Storeable createFor(Table table, List<?> values)
            throws ColumnFormatException, IndexOutOfBoundsException {
        return provider.createFor(table, values);
    }

    @Override
    public List<String> getTableNames() {
        return provider.getTableNames();
    }
}
