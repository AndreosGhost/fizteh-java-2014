package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.registry.Registry;

interface IRemoteTableProviderFactory extends Remote, Closeable {
    RemoteTableProviderStub obtainRemoteProvider() throws IOException;

    default void establishStorage(String localDatabaseRoot) throws IOException {
        establishStorage(localDatabaseRoot, Registry.REGISTRY_PORT);
    }

    void establishStorage(String localDatabaseRoot, int port) throws IOException;
}
