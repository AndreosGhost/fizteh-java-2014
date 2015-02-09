package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.UnexpectedRemoteException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.util.List;

/**
 * Remote table provider that is created at server (once) and can be exploited by clients.
 */
class RemoteTableProviderImpl extends UnicastRemoteObject implements IRemoteTableProvider {
    // Even if we keep the match (pure table -> table stub), the same stubs sent to the client will be
    // deserealized as different instances. So, we do not keep this match.

    private final TableProvider provider;

    public RemoteTableProviderImpl(TableProvider provider) throws RemoteException {
        this.provider = provider;
    }

    private RemoteTableStub wrapInStub(Table table) throws UnexpectedRemoteException {
        try {
            return table == null ? null : new RemoteTableStub(new RemoteTableImpl(table), table.getName());
        } catch (RemoteException exc) {
            throw new UnexpectedRemoteException(exc);
        }
    }

    @Override
    public RemoteTableStub getTable(String name) {
        return wrapInStub(provider.getTable(name));
    }

    @Override
    public RemoteTableStub createTable(String name, List<Class<?>> columnTypes) throws IOException {
        return wrapInStub(provider.createTable(name, columnTypes));
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
