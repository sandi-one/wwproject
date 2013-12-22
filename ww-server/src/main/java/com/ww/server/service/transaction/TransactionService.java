package com.ww.server.service.transaction;

import com.ww.server.ExceptionHandler;
import com.ww.server.events.Event;
import com.ww.server.events.EventRegistry;
import com.ww.server.persistence.TransactionScope;
import com.ww.server.service.Service;
import com.ww.server.service.exception.ServiceErrors;
import com.ww.server.service.exception.ServiceException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import org.hibernate.Session;

/**
 *
 * @author sandy
 */
public class TransactionService extends Service {

    protected static final Logger _log = Logger.getLogger(TransactionService.class.getName());
    private static final CommitTransactionHandler commitTransactionDispatcher = EventRegistry.getDispatcher(CommitTransactionHandler.class);
    private static final Event<CommitTransactionHandler> commitTransactionEvent = EventRegistry.getEvent(CommitTransactionHandler.class);

    private TransactionScope.Transaction databaseTransaction = null;
    private String tranactionId;

    public String getTranactionId() {
        return tranactionId;
    }

    public boolean isActiveTransaction() {
        return tranactionId != null;
    }

    @SuppressWarnings("deprecation")
    public void beginTransaction() throws ServiceException {
        try {
            databaseTransaction = new TransactionScope.Transaction();
            if (tranactionId == null) {
                tranactionId = UUID.randomUUID().toString();
            }
            databaseTransaction.registerSynchronization(new Synchronization() {

                @Override
                public void beforeCompletion() {
                }

                @Override
                public void afterCompletion(int status) {
                    if (Status.STATUS_COMMITTED == status) {
                        TransactionScope.runInScopeSafety(new TransactionScope.Command<Void>() {

                            @Override
                            public Void executeInScope(Session session) throws Exception {
                                commitTransactionDispatcher.onCommit();
                                return null;
                            }
                        }, ExceptionHandler.DEFAULT);
                    }
                    commitTransactionEvent.resetDeferredEvents();
                }
            });

        } catch (Exception ex) {
            throw new ServiceException(ServiceErrors.TRANSACTION_BEGIN_EXCEPTION, ex.getMessage());
        }
    }

    public void registerSynchronization(Synchronization synchronization)
            throws ServiceException {
        if (databaseTransaction != null && databaseTransaction.isActive()) {
            databaseTransaction.registerSynchronization(synchronization);
        } else if (TransactionScope.getSessionForScope().getTransaction().isActive()) {
            TransactionScope.getSessionForScope().getTransaction().registerSynchronization(synchronization);
        } else {
            throw new ServiceException(ServiceErrors.TRANSACTION_REGISTER_SYNCHRONIZATION_ERROR,
                    "Transaction is not active");
        }
    }

    public void commit() throws ServiceException {
        try {
            if (databaseTransaction != null && databaseTransaction.isActive()) {
                databaseTransaction.commit();
            }
        } catch (Exception ex) {
            throw new ServiceException(ServiceErrors.TRANSACTION_COMMIT_EXCEPTION, ex.getMessage());
        } finally {
            tranactionId = null;
        }
    }

    public void rollback() throws ServiceException {
        try {
            if (databaseTransaction != null && databaseTransaction.isActive()) {
                databaseTransaction.rollback();
            }
        } catch (Exception ex) {
            throw new ServiceException(ServiceErrors.TRANSACTION_ROLLBACK_EXCEPTION, ex.getMessage());
        } finally {
            tranactionId = null;
        }
    }
}
