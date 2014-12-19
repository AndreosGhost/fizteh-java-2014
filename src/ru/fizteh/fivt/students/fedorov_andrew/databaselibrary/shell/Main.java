package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.Database;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote.RemoteDatabaseStorage;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.ClientServerGeneralState;

import java.rmi.registry.Registry;

class Main {
    //java -Dfizteh.db.dir=/home/phoenix/test/DB ru.fizteh.fivt.students.fedorov_andrew.databaselibrary
    // .shell.Main

    private static final String PATH_PROPERTY = "fizteh.db.dir";

    public static void main(String[] args) {
        try {
            Shell<ClientServerGeneralState> shell = new Shell<>(
                    new ClientServerGeneralState() {
                        @Override
                        protected Database obtainNewActiveDatabase() throws Exception {
                            RemoteDatabaseStorage storage = new RemoteDatabaseStorage();
                            RemoteTableProvider provider =
                                    storage.connect("localhost", Registry.REGISTRY_PORT);
                            return new Database(provider, "", getOutputStream());
                        }
                    });
            int exitCode;
            if (args.length == 0) {
                exitCode = shell.run(System.in);
            } else {
                exitCode = shell.run(args);
            }

            System.exit(exitCode);
        } catch (TerminalException exc) {
            // Already handled.
            System.exit(1);
        } catch (Exception exc) {
            //            exc.printStackTrace();
            System.out.println(exc.getMessage());
            System.exit(1);
        }
    }
}
