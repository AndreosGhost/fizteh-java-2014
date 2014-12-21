package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.AutoCloseableTableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.DBTableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * My remote factory.
 */
public class RemoteTableProviderFactoryImpl extends UnicastRemoteObject
        implements IRemoteTableProviderFactory {

    static final String FACTORY_NAME = "/Database";

    private Registry registry;

    private AutoCloseableTableProviderFactory factory;

    /**
     * Server-local table provider.
     */
    private TableProvider provider;

    /**
     * Server-local singleton implementation of remote table provider that is wrapped in stub.
     */
    private IRemoteTableProvider remoteProvider;

    public RemoteTableProviderFactoryImpl() throws RemoteException {
    }

    public synchronized TableProvider getProvider() {
        return provider;
    }

    private boolean isBound() {
        return registry != null;
    }

    private void requireBound() throws IllegalStateException {
        if (!isBound()) {
            throw new IllegalStateException("Factory is not established yet.");
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (!isBound()) {
            return;
        }
        try {
            registry.unbind(FACTORY_NAME);
            factory.close();
        } catch (NotBoundException exc) {
            throw new IllegalStateException("The factory has not been established");
        } finally {
            factory = null;
            provider = null;
            remoteProvider = null;
            registry = null;
        }
    }

    @Override
    public synchronized RemoteTableProviderStub obtainRemoteProvider() throws IOException {
        requireBound();
        return new RemoteTableProviderStub(remoteProvider);
    }

    @Override
    public synchronized TableProvider establishStorage(String localDatabaseRoot, int port)
            throws IOException {
        if (isBound()) {
            throw new IllegalStateException("Factory already established");
        }

        try {
            factory = new DBTableProviderFactory();
            provider = factory.create(localDatabaseRoot);

            try {
                registry = LocateRegistry.getRegistry(port);
                registry.rebind(FACTORY_NAME, this);

            } catch (RemoteException exc) {
                registry = LocateRegistry.createRegistry(port);
                registry.rebind(FACTORY_NAME, this);
            }

            remoteProvider = new RemoteTableProviderImpl(provider);

            return provider;
        } catch (Exception exc) {
            Log.log(RemoteTableProviderFactoryImpl.class, exc, "Got exception on establishment");
            try {
                if (factory != null) {
                    factory.close();
                }
                factory = null;
                provider = null;
                registry = null;
                remoteProvider = null;
            } catch (Exception ignored) {
                Log.log(
                        RemoteTableProviderFactoryImpl.class,
                        ignored,
                        "Failed to cleanup after getting exception on establishment");
            }
            throw exc;
        }
    }
}
