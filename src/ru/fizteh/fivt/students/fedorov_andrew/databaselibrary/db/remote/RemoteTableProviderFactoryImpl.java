package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.AutoCloseableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.ProviderWrap;
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
    /**
     * Local provider that is final for all time.
     */
    private final TableProvider localProvider;
    private Registry registry;
    /**
     * Provider wrap that can be closed.
     */
    private AutoCloseableProvider providerWrap;
    /**
     * Server-local singleton implementation of remote table providerWrap that is wrapped in stub.
     */
    private IRemoteTableProvider remoteProvider;

    public RemoteTableProviderFactoryImpl(TableProvider provider) throws RemoteException {
        this.localProvider = provider;
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
            providerWrap.close();
            registry.unbind(FACTORY_NAME);
        } catch (NotBoundException exc) {
            throw new IllegalStateException("The factory has not been established");
        } finally {
            providerWrap = null;
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
    public synchronized void establishStorage(String localDatabaseRoot, int port) throws IOException {
        if (isBound()) {
            throw new IllegalStateException("Factory already established");
        }

        try {
            try {
                registry = LocateRegistry.getRegistry(port);
                registry.rebind(FACTORY_NAME, this);

            } catch (RemoteException exc) {
                registry = LocateRegistry.createRegistry(port);
                registry.rebind(FACTORY_NAME, this);
            }

            providerWrap = new ProviderWrap(localProvider);
            remoteProvider = new RemoteTableProviderImpl(providerWrap);
        } catch (Exception exc) {
            Log.log(RemoteTableProviderFactoryImpl.class, exc, "Got exception on establishment");
            try {
                providerWrap.close();
                providerWrap = null;
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
