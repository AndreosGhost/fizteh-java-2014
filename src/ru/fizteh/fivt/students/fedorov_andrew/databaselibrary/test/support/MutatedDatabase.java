package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test.support;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.Database;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.DatabaseIOException;

import java.io.IOException;

public class MutatedDatabase extends Database {
    private int commitCallsLeft;

    public MutatedDatabase(TableProvider provider, String dbDirectory, int commitCallsLeft) {
        super(provider, dbDirectory, System.out);
        this.commitCallsLeft = commitCallsLeft;
    }

    @Override
    public int commit() throws IOException {
        if (commitCallsLeft == 0) {
            throw new DatabaseIOException("Fail on commit [test mode]");
        }

        commitCallsLeft--;
        return super.commit();
    }
}
