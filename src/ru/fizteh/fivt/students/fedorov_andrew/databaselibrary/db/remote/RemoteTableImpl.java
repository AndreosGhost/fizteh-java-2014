package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Remote table object that is stored on server and available through RMI mechanism.
 */
class RemoteTableImpl extends UnicastRemoteObject implements IRemoteTable {

    private final Table table;

    public RemoteTableImpl(Table table) throws RemoteException {
        this.table = table;
    }

    public Table getPureTable() {
        return table;
    }

    @Override
    public String getName() throws RemoteException {
        return table.getName();
    }

    @Override
    public Storeable get(String key) throws RemoteException, IllegalStateException {
        return table.get(key);
    }

    @Override
    public Storeable put(String key, Storeable value) throws ColumnFormatException, RemoteException {
        return table.put(key, value);
    }

    @Override
    public Storeable remove(String key) throws RemoteException {
        return table.remove(key);
    }

    @Override
    public int size() throws RemoteException {
        return table.size();
    }

    @Override
    public List<String> list() throws RemoteException {
        return table.list();
    }

    @Override
    public int commit() throws IOException {
        return table.commit();
    }

    @Override
    public int rollback() throws RemoteException {
        return table.rollback();
    }

    @Override
    public int getNumberOfUncommittedChanges() throws RemoteException {
        return table.getNumberOfUncommittedChanges();
    }

    @Override
    public int getColumnsCount() throws RemoteException {
        return table.getColumnsCount();
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws RemoteException, IndexOutOfBoundsException {
        return table.getColumnType(columnIndex);
    }
}
