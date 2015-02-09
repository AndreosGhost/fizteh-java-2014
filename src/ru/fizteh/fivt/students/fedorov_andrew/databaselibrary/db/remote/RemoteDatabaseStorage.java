package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.storage.structured.RemoteTableProviderFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to connect to a remote database storage and obtain providers.
 */
public class RemoteDatabaseStorage implements RemoteTableProviderFactory {
    // There is no read-write sync, because obtaining many providers seem to be not so popular as
    // obtaining many tables.

    private final Map<String, RemoteTableProviderStub> providerStubs = new HashMap<>();

    private boolean providerStubClosedByMe = false;

    public RemoteDatabaseStorage() {
    }

    private String makeLocation(String hostname, int port) throws UnknownHostException {
        return InetAddress.getByName(hostname).getHostAddress() + ":" + port;
    }

    void onProviderStubClosed(String locationInStorage) {
        synchronized (providerStubs) {
            if (!providerStubClosedByMe) {
                providerStubs.remove(locationInStorage);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        synchronized (providerStubs) {
            providerStubClosedByMe = true;
            providerStubs.values().forEach(RemoteTableProviderStub::close);
            providerStubs.clear();
        }
    }

    private RemoteTableProviderStub tryReplaceProviderStub(String location,
                                                           RemoteTableProviderStub providerStub) {
        if (providerStub == null) {
            return null;
        }

        synchronized (providerStubs) {
            RemoteTableProviderStub oldStub = providerStubs.get(location);

            if (oldStub == null) {
                providerStubs.put(location, providerStub);
                providerStub.bindToStorage(this, location);
                return providerStub;
            } else {
                return oldStub;
            }
        }
    }

    @Override
    public RemoteTableProvider connect(String hostname, int port) throws IOException {
        Registry registry = LocateRegistry.getRegistry(hostname, port);
        try {
            IRemoteTableProviderFactory factory = (IRemoteTableProviderFactory) registry
                    .lookup(RemoteTableProviderFactoryImpl.FACTORY_NAME);
            return tryReplaceProviderStub(makeLocation(hostname, port), factory.obtainRemoteProvider());
        } catch (NotBoundException exc) {
            // This cannot happen.
            throw new RuntimeException(exc);
        }
    }
}
