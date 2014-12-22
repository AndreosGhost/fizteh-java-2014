package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.servlet.transactions.Transaction;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.servlet.transactions.TransactionPool;

import static org.junit.Assert.*;

public class TransactionPoolTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testTooManyTransactions() throws Exception {
        int count = 10;
        TransactionPool pool = new TransactionPool(count, 100000L);

        Transaction[] transactions = new Transaction[count];

        for (int i = 0; i < 10; i++) {
            Transaction transaction = pool.newTransaction();
            int id = transaction.getTransactionID();
            assertNull("Duplicate id", transactions[id]);
            transactions[id] = transaction;
            pool.releaseTransaction(transaction);
        }

        exception.expect(IllegalStateException.class);
        exception.expectMessage("No more free transaction IDs");
        pool.newTransaction();
    }

    @Test
    public void testRemoveTransactionAndPerformAction() throws Exception {
        TransactionPool pool = new TransactionPool(100500, 100000L);
        Transaction tr = pool.newTransaction();
        pool.killTransaction(tr);

        exception.expect(IllegalStateException.class);
        exception.expectMessage("You can execute actions only in active state");

        tr.executeAction(null);
    }

    @Test
    public void testTransactionDiesAfterSomeTime() throws Exception {
        TransactionPool pool = new TransactionPool(100, 100L);
        Transaction tr = pool.newTransaction();
        pool.releaseTransaction(tr);

        Thread.sleep(500L);

        exception.expect(IllegalStateException.class);
        exception.expectMessage("You can execute actions only in active state");
        tr.executeAction(null);
    }

    @Test
    public void testTransactionDiesAfterSomeTime1() throws Exception {
        TransactionPool<Object> pool = new TransactionPool(100, 100L);
        Transaction tr = pool.newTransaction();
        int id = tr.getTransactionID();
        pool.releaseTransaction(tr);

        Thread.sleep(500L);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Transaction not found: " + id);
        tr = pool.obtainTransaction(id);
        try {
            tr.executeAction(null);
        } finally {
            pool.releaseTransaction(tr);
        }
    }
}
