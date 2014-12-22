package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.servlet.transactions;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.InvalidatedObjectException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController.KillLock;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController.UseLock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

/**
 * Thread-safe transaction pool.
 */
public class TransactionPool<T> {
    private static final int RANDOM_ID_ATTEMPTS = 10;
    private final int idUpperBound;
    private final Timer timer;

    private final ValidityController validityController = new ValidityController();

    /**
     * Mapping between transaction IDs and transactions.
     */
    private final Map<Integer, Transaction<T>> transactionMap = new HashMap<>();

    /**
     * For operating with transactionMap.
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * Time to live for each transaction. If at least this time passes after a transaction was last accessed,
     * it must be killed.
     */
    private final long transactionTimeToLive;

    public TransactionPool(int idUpperBound, long transactionTimeToLive) {
        this.idUpperBound = idUpperBound;
        this.transactionTimeToLive = transactionTimeToLive;
        timer = new Timer();
        timer.schedule(new PeriodicCleanup(), transactionTimeToLive, transactionTimeToLive / 2);
    }

    public void closePool() {
        try (KillLock killLock = validityController.useAndKill()) {
            timer.cancel();
            cleanup((transaction) -> true);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            closePool();
        } catch (InvalidatedObjectException exc) {
            // Ignore it.
        }
    }

    /**
     * Perform pool cleanup.
     * @param filter
     *         if true, transaction must be killed. Otherwise it is not effected.
     */
    private void cleanup(Predicate<Transaction<T>> filter) {
        try (UseLock useLock = validityController.use()) {
            lock.writeLock().lock();
            try {
                Iterator<Entry<Integer, Transaction<T>>> transactionsIter =
                        transactionMap.entrySet().iterator();
                while (transactionsIter.hasNext()) {
                    Transaction<T> transaction = transactionsIter.next().getValue();
                    transaction.obtainWriteLock();
                    try {
                        if (filter.test(transaction)) {
                            try {
                                transactionsIter.remove();
                                transaction.destroy();
                            } catch (Exception exc) {
                                Log.log(
                                        TransactionPool.class,
                                        exc,
                                        "Error while killing transaction: " + transaction.getTransactionID());
                            }
                        }
                    } finally {
                        transaction.releaseWriteLock();
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public void killTransaction(Transaction<T> transaction) throws Exception {
        try (UseLock useLock = validityController.use()) {
            lock.writeLock().lock();
            try {
                if (transactionMap.get(transaction.getTransactionID()) != transaction) {
                    throw new IllegalArgumentException("Transaction not from this pool");
                }
                transactionMap.remove(transaction.getTransactionID());
                try {
                    transaction.destroy();
                } finally {
                    releaseTransaction(transaction);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public void releaseTransaction(Transaction<T> transaction) {
        try (UseLock useLock = validityController.use()) {
            transaction.releaseWriteLock();
        }
    }

    public Transaction<T> obtainTransaction(int transactionID) throws IllegalArgumentException {
        try (UseLock useLock = validityController.use()) {
            lock.readLock().lock();
            try {
                Transaction<T> transaction = transactionMap.get(transactionID);
                if (transaction == null) {
                    throw new IllegalArgumentException("Transaction not found: " + transactionID);
                }

                transaction.obtainWriteLock();
                return transaction;
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    public Transaction<T> newTransaction() throws Exception {
        try (UseLock useLock = validityController.use()) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            lock.writeLock().lock();
            try {
                if (idUpperBound == transactionMap.size()) {
                    throw new IllegalStateException("No more free transaction IDs");
                }
                int transactionID = random.nextInt(idUpperBound);
                int times = 1;
                while (transactionMap.containsKey(transactionID) && times < RANDOM_ID_ATTEMPTS) {
                    transactionID = random.nextInt(idUpperBound);
                    times++;
                }
                if (transactionMap.containsKey(transactionID)) {
                    for (transactionID = 0; transactionID < idUpperBound; transactionID++) {
                        if (!transactionMap.containsKey(transactionID)) {
                            break;
                        }
                    }
                }

                Transaction<T> transaction = new Transaction<>(transactionID);
                transaction.obtainWriteLock();

                transaction.init();
                transactionMap.put(transactionID, transaction);
                return transaction;
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    class PeriodicCleanup extends TimerTask {
        @Override
        public void run() {
            try {
                long currentTime = System.currentTimeMillis();
                cleanup(
                        (transaction) -> currentTime - transaction.getLastAccessTime()
                                         >= transactionTimeToLive);
            } catch (Exception exc) {
                Log.log(PeriodicCleanup.class, exc, "Failed to perform periodic cleanup");
            }
        }
    }
}
