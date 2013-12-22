package com.ww.server.persistence;

import com.ww.server.exception.ActionErrors;
import com.ww.server.exception.ActionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sandy
 */
public class InnodbDeadlockRetrier {

    protected static final Logger _log = Logger.getLogger(InnodbDeadlockRetrier.class.getName());
    private static final int TRANSACTION_RETRIES_COUNT = 3;

    public static interface Command {

        public void execute() throws ActionException;

        public void handleError() throws ActionException;

    }

    public static void executeCommand(InnodbDeadlockRetrier.Command command) throws ActionException {
        int failedTransactionsCount = 0;
        while (true) {
            try {
                command.execute();
                break;
            } catch (Exception ex) {
                if (!TransactionScope.checkExceptionToRestartTransaction(ex)) {
                    if (ex instanceof ActionException) {
                        throw (ActionException) ex;
                    } else if (ex instanceof RuntimeException) {
                        throw (RuntimeException) ex;
                    } else {
                        throw new ActionException(ActionErrors.UNKNOWN_SERVER_ERROR, ex);
                    }
                }

                int transactionRestartCount = TRANSACTION_RETRIES_COUNT;
                if (failedTransactionsCount >= transactionRestartCount) {
                    _log.log(Level.SEVERE, "Transaction retrying limit reached");
                    throw new ActionException(ActionErrors.DEADLOCK_LIMIT_REACHED);
                }

                failedTransactionsCount++;
                _log.log(Level.SEVERE, "Retrying transaction. Attempt #" + failedTransactionsCount);
                command.handleError();
                continue;
            }
        }
    }

}
