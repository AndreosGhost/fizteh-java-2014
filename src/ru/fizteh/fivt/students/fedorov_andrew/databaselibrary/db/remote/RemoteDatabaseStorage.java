package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.storage.structured.RemoteTableProviderFactory;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Class used to connect to a remote database storage and obtain providers.
 */
public class RemoteDatabaseStorage implements RemoteTableProviderFactory {

    public RemoteDatabaseStorage() {

    }

    @Override
    public RemoteTableProvider connect(String hostname, int port) throws IOException {
        Registry registry = LocateRegistry.getRegistry(hostname, port);
        try {
            IRemoteTableProviderFactory factory = (IRemoteTableProviderFactory) registry
                    .lookup(RemoteTableProviderFactoryImpl.FACTORY_NAME);
            return factory.obtainRemoteProvider();
        } catch (NotBoundException exc) {
            // This cannot happen.
            throw new RuntimeException(exc);
        }
    }
}
