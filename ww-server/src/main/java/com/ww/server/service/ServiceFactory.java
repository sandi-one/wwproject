package com.ww.server.service;

import com.ww.server.service.transaction.TransactionService;
import com.ww.server.util.Lazy;

/**
 *
 * @author sandy
 */
public class ServiceFactory implements Factory {

    private final Lazy<TransactionService> transactionService;

    public ServiceFactory() {
        this.transactionService = Lazy.async(TransactionService.class);
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService.get();
    }
}
