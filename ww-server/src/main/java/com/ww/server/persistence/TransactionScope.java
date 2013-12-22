package com.ww.server.persistence;

import com.ww.server.ExceptionHandler;
import com.ww.server.enums.TransactionIsolation;
import com.ww.server.persistence.exception.AsyncDatabaseChangedException;
import com.ww.server.persistence.exception.PersistenceException;
import com.ww.server.persistence.exception.UnexpectedException;
import com.ww.server.util.HibernateUtil;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.Synchronization;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;

/**
 *
 * @author sandy
 */
public class TransactionScope {

    private static final Logger _log = Logger.getLogger(TransactionScope.class.getName());

    public static abstract class Command<T> {

        private TransactionIsolation isolation;

        public Command() {
            this(null);
        }

        public Command(TransactionIsolation transactionIsolation) {
            isolation = transactionIsolation;
        }

        public abstract T executeInScope(Session session) throws Exception;

        public void handleException(Exception ex) throws Exception {
            // do nothing by default
        }
    }

    public static class Transaction {

        private org.hibernate.Transaction transaction = null;

        public Transaction() {
            begin();
        }

        private void begin() {
            Session currentSession = getSessionForScope();
            if (!currentSession.getTransaction().isActive()) {
                transaction = currentSession.beginTransaction();
            }
        }

        public void commitIfNotNestead() {
            // If we opened a transaction -- we do close current transaction
            //  (despite of that is it created by us or not)
            if (transaction != null) {
                commit();
            }
        }

        public void commitAndBegin() {
            commit();
            begin();
        }

        public void rollbackAndBegin() {
            rollback();
            begin();
        }

        public void commit() {
            org.hibernate.Transaction currentTransaction = getSessionForScope().getTransaction();
            if (currentTransaction.isActive()) {
                currentTransaction.commit();
            }
        }

        private org.hibernate.Transaction getActiveTransaction() {
            return getSessionForScope().getTransaction();
        }

        public void rollback() {
            Session currentSession = getSessionForScope();
            org.hibernate.Transaction transact = currentSession.getTransaction();
            if (transact != null && transact.isActive() && !transact.wasRolledBack()) {
                try {
                    transact.rollback();
                } catch (HibernateException ex) {
                    // if we can not rollback transaction, it is probably problems with connection
                    _log.log(Level.SEVERE, "Rollback error", ex);
                }
            }
        }

        public boolean isNested() {
            return transaction == null;
        }

        public boolean wasRolledBack() throws HibernateException {
            return getActiveTransaction().wasRolledBack();
        }

        public boolean wasCommitted() throws HibernateException {
            return getActiveTransaction().wasCommitted();
        }

        public boolean isActive() throws HibernateException {
            return getActiveTransaction().isActive();
        }

        public void setTimeout(int i) {
            getActiveTransaction().setTimeout(i);
        }

        public void registerSynchronization(Synchronization synchronization) {
            getActiveTransaction().registerSynchronization(synchronization);
        }
    }

    @SuppressWarnings("deprecation")
    public static Session getSessionForScope() {
        return HibernateUtil.getSession();
    }

    public static boolean checkExceptionToRestartTransaction(Throwable ex) {
        return checkExceptionToRestartTransaction(ex, 0);
    }

    public static boolean checkExceptionToRestartTransaction(Throwable ex, int depth) {
        if (ex == null) {
            return false;
        } else if (ex instanceof SQLException) {
            int errorCode = ((SQLException) ex).getErrorCode();
            // 1205 - error code of "Lock wait timeout exceeded; try restarting transaction"
            // 1213 - error code of "Deadlock found when trying to get lock; try restarting transaction"
            if (errorCode == 1205 || errorCode == 1213) {
                return true;
            } else {
                return false;
            }
        } else if (ex instanceof StaleStateException || ex instanceof LockAcquisitionException
                || ex instanceof AsyncDatabaseChangedException) {
            return true;
        } else {
            // insurance on infinite recursion
            if (depth < 10) {
                return checkExceptionToRestartTransaction(ex.getCause(), ++depth);
            } else {
                return false;
            }
        }
    }

    public static <T> T runInSeparateScope(final Command<T> command) throws Exception {
        return runInSeparateScope(command, false);
    }

    public static <T> T runInSeparateScope(final Command<T> command, final boolean isAsync) throws Exception {
        final T[] resultHolder = (T[]) new Object[1];
        final Exception[] exceptionHolder = {null};
        Thread runThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    resultHolder[0] = runInScope(command);
                } catch (Exception ex) {
                    if (!isAsync) {
                        exceptionHolder[0] = ex;
                    } else {
                        _log.log(Level.SEVERE, "Running in asynchronous separate scope was failed!", ex);
                    }
                }
            }
        });
        runThread.start();
        if (!isAsync) {
            try {
                runThread.join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                runThread.interrupt();
                throw ex;
            }
            if (exceptionHolder[0] != null) {
                throw exceptionHolder[0];
            }
        }

        return resultHolder[0];
    }

    @SuppressWarnings("deprecation")
    public static <T> T runInScope(final Command<T> command) throws Exception {
        int restartsCount = 0;
        while (true) {
            final Session session = getSessionForScope();
            Transaction transaction = null;
            try {
                transaction = new Transaction();
                T result;
                if (command.isolation != null) {
                     result = executeInOtherIsolationLevelScope(command.isolation, new Callable<T>() {
                        @Override
                        public T call() throws Exception {
                            return command.executeInScope(session);
                        }
                    });
                }  else {
                    result = command.executeInScope(session);
                }
                transaction.commitIfNotNestead();
                return result;
            } catch (Exception ex) {
                boolean isNestedTransaction = (transaction == null) || transaction.isNested();
                if (!isNestedTransaction) {
                    transaction.rollback();
                }
                if (restartsCount < 10 && !isNestedTransaction
                        && checkExceptionToRestartTransaction(ex)) {
                    //_logger.setLevel(Level.DEBUG);
                    _log.log(Level.FINE, "Deadlock or lock wait timeout exceeded. Restarting transaction."
                            + " Attempt #" + (++restartsCount), ex);
                    command.handleException(ex);
                } else {
                    throw ex;
                }
            } finally {
                // get current scope session, it can be not same as session on start of scope
                // if transaction was commited forsely and restarted with new session
                Session currentSession = getSessionForScope();
                if (currentSession != null && currentSession.isOpen()
                        && !currentSession.getTransaction().isActive()) {
                    currentSession.close();
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static <T> T runInScopeSafety(Command<T> command, ExceptionHandler<T> exceptionHandler) {
        try {
            return runInScope(command);
        } catch (Exception ex) {
            if (exceptionHandler == null) {
                ExceptionHandler.DEFAULT.handleException(ex);
                return null;
            } else {
                return exceptionHandler.handleException(ex);
            }
        }
    }

    public static <T> T executeInReadCommitedIsolationScope(Callable<T> callable) throws UnexpectedException {
        return executeInOtherIsolationLevelScope(TransactionIsolation.READ_COMMITTED, callable);
    }

    public static <T> T executeInOtherIsolationLevelScope(TransactionIsolation otherIsolation, Callable<T> callable) throws UnexpectedException {
        try {
            // some tick to avoid lock during INSERT...SELECT
            // NOTE! It will work only with binlog_format = MIXED
            Persistence.executeSQLQuery("commit");
            Persistence.executeSQLQuery("SET @old_tx_isolation = @@tx_isolation");
            Persistence.executeSQLQuery("SET SESSION tx_isolation = '" + otherIsolation.toString() + "'");

            return callable.call();
        } catch (Exception ex) {
            if (ex instanceof PersistenceException) {
                throw (PersistenceException) ex;
            } else {
                throw new UnexpectedException(ex);
            }
        } finally {
            Persistence.executeSQLQuery("SET SESSION tx_isolation = @old_tx_isolation");
        }
    }
}
