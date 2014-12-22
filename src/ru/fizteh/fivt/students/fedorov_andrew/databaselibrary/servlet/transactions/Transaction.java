package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.servlet.transactions;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.parallel.ControllableAgent;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.parallel.ControllableRunner;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.parallel.ExceptionFreeRunnable;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * Transaction object that executes some action within the same thread which sleep between actions.<br/>
 * Can be used only by one thread at a time.<br/>
 * You can reuse this object many times.
 */
public class Transaction<T> {
    /**
     * ID of this transaction.
     */
    private final int transactionID;
    /**
     * Action performer. Run by hostRunner. Sleeps between action performing.
     */
    private final ActionPerformer performer = new ActionPerformer();
    /**
     * Lock for thread-safe use.
     */
    private final WriteLock useLock = new ReentrantReadWriteLock(true).writeLock();
    /**
     * Host runner to run the performer.
     */
    private final ControllableRunner hostRunner = new ControllableRunner();
    /**
     * Action to perform next. Nullable.
     */
    private Action action;
    /**
     * Result of performed action. Can be null.
     */
    private Object result;
    /**
     * If true, execution must be stopped.
     */
    private State state = State.NotStarted;
    /**
     * Error occurred during action performing. Null, if all is ok.
     */
    private Exception error;
    /**
     * Timestamp when this transaction was last accessed. {@link TransactionPool} decides when to kill this
     * transaction looking at this timestamp.
     */
    private long lastAccessTime;
    private T extraData;

    public Transaction(int transactionID) {
        this.transactionID = transactionID;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public T getExtraData() {
        useLock.lock();
        try {
            return extraData;
        } finally {
            useLock.unlock();
        }
    }

    public void setExtraData(T extraData) {
        useLock.lock();
        try {
            this.extraData = extraData;
        } finally {
            useLock.unlock();
        }
    }

    /**
     * This method is called by TransactionPool to decide whether to kill this transaction or not.
     * @return
     */
    long getLastAccessTime() {
        useLock.lock();
        try {
            return lastAccessTime;
        } finally {
            useLock.unlock();
        }
    }

    /**
     * This method is called by TransactionPool when this transaction is created.<br/>
     * @throws Exception
     */
    void init() throws Exception {
        lastAccessTime = System.currentTimeMillis();
        if (state != State.NotStarted) {
            throw new IllegalStateException("Already initialized");
        }
        state = State.Active;
        hostRunner.createAndAssign(performer);
        new Thread(hostRunner, "Transaction " + transactionID).start();

        hostRunner.waitUntilPause();
    }

    /**
     * This method is called by TransactionPool when this transaction must be killed.<br/>
     * Lock is expected to be obtained outside this method.
     * @throws Exception
     */
    void destroy() throws Exception {
        if (state == State.Active) {
            state = State.Stopped;
            hostRunner.waitUntilEndOfWork();
        } else {
            state = State.Stopped;
        }
    }

    void obtainWriteLock() {
        useLock.lock();
    }

    void releaseWriteLock() {
        useLock.unlock();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }

    public <T> T executeAction(Action<T> action) throws Exception {
        useLock.lock();
        try {
            lastAccessTime = System.currentTimeMillis();
            if (state != State.Active) {
                throw new IllegalStateException("You can execute actions only in active state");
            }

            this.action = action;

            hostRunner.continueWork();
            hostRunner.waitUntilPause();

            if (error != null) {
                throw error;
            }
            return (T) result;
        } finally {
            useLock.unlock();
        }
    }

    enum State {
        NotStarted,
        Stopped,
        Active
    }

    public interface Action<T> {
        T perform() throws Exception;
    }

    class ActionPerformer implements ExceptionFreeRunnable {
        @Override
        public void runWithFreedom(ControllableAgent agent) throws Exception, AssertionError {
            while (true) {
                agent.notifyAndWait();

                if (state == State.Stopped) {
                    return;
                }

                if (action == null) {
                    throw new IllegalStateException("Cannot run without action");
                }

                // Exception can occur on this step.
                try {
                    error = null;
                    result = action.perform();
                } catch (Exception exc) {
                    error = exc;
                } finally {
                    action = null;
                }
            }
        }
    }
}
