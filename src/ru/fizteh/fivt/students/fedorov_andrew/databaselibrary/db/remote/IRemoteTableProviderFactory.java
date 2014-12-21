package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.TableProvider;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.registry.Registry;

interface IRemoteTableProviderFactory extends Remote, Closeable {
    RemoteTableProviderStub obtainRemoteProvider() throws IOException;

    default TableProvider establishStorage(String localDatabaseRoot) throws IOException {
        return establishStorage(localDatabaseRoot, Registry.REGISTRY_PORT);
    }

    TableProvider establishStorage(String localDatabaseRoot, int port) throws IOException;
}
