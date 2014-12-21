package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.InvalidatedObjectException;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Convenience class to control one's validity.
 */
public final class ValidityController {
    private final ReadWriteLock validityLock = new ReentrantReadWriteLock(true);
    private boolean valid = true;

    private void checkValid() {
        if (!valid) {
            throw new InvalidatedObjectException("Object has been invalidated");
        }
    }

    /**
     * Returns activated lock on the use of the object. While object is used, nobody can invalidate it
     * (except
     * the host thread of this lock).
     * @throws InvalidatedObjectException
     *         if this object has been already invalidated.
     */
    public UseLock use() throws InvalidatedObjectException {
        validityLock.readLock().lock();
        try {
            checkValid();
            validityLock.readLock().lock();
            return new UseLock();
        } finally {
            validityLock.readLock().unlock();
        }
    }

    /**
     * Returns activated unique lock for the use of the object. After use object is invalidated.
     * @throws InvalidatedObjectException
     *         if this object has been already invalidated.
     */
    public KillLock useAndKill() throws InvalidatedObjectException {
        validityLock.writeLock().lock();
        try {
            checkValid();
            validityLock.writeLock().lock();
            return new KillLock();
        } finally {
            validityLock.writeLock().unlock();
        }
    }

    public interface ValidityLock extends AutoCloseable {
        @Override
        void close();
    }

    public class UseLock implements ValidityLock {
        private boolean allowMultipleCloseAttempts = false;

        @Override
        public void close() {
            if (!allowMultipleCloseAttempts) {
                validityLock.readLock().unlock();
            }
        }

        public KillLock obtainKillLockInstead() {
            close();
            allowMultipleCloseAttempts = true;
            // Further close() calls (e.g., from try-with-resources) will be ignored for UseLock.
            return useAndKill();
        }
    }

    public class KillLock implements ValidityLock {
        @Override
        public void close() {
            valid = false;
            validityLock.writeLock().unlock();
        }
    }

}
