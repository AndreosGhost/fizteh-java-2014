package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.AutoCloseableTableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.DBTableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.DBShellGeneralState;

class Main {
    //java -Dfizteh.db.dir=/home/phoenix/test/DB ru.fizteh.fivt.students.fedorov_andrew.databaselibrary
    // .shell.Main

    private static final String PATH_PROPERTY = "fizteh.db.dir";

    public static void main(String[] args) {
        try (AutoCloseableTableProviderFactory factory = new DBTableProviderFactory()) {
            String databaseRoot = System.getProperty(SingleDatabaseShellState.DB_DIRECTORY_PROPERTY_NAME);
            TableProvider provider = factory.create(databaseRoot);

            Shell<DBShellGeneralState> shell = new Shell<>(new DBShellGeneralState(provider, databaseRoot));
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
            Log.log(Main.class, exc);
            System.out.println(exc.getMessage());
            System.exit(1);
        }
    }
}
