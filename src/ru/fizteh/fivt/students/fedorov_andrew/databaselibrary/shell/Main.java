package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.Database;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.ServerState;

class Main {
    //java -Dfizteh.db.dir=/home/phoenix/test/DB ru.fizteh.fivt.students.fedorov_andrew.databaselibrary
    // .shell.Main

    private static final String PATH_PROPERTY = "fizteh.db.dir";

    public static void main(String[] args) {
        try (DatabaseFactory factory = new DatabaseFactory()) {
            Shell<ServerState> shell = new Shell<>(
                    new ServerState(
                            () -> new SingleDatabaseShellState() {
                                @Override
                                protected Database obtainNewActiveDatabase() throws Exception {
                                    return factory.obtainDatabase(getOutputStream());
                                }
                            }));
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
            System.out.println(exc.getMessage());
            System.exit(1);
        }
    }
}
