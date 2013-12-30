package com.ww.server.service;

import com.ww.server.service.authentication.AuthenticationService;
import com.ww.server.service.transaction.TransactionService;
import com.ww.server.util.Lazy;

/**
 *
 * @author sandy
 */
public class ServiceFactory implements Factory {

    private final Lazy<TransactionService> transactionService;
    private final Lazy<AuthenticationService> authenticationService;

    public ServiceFactory() {
        this.transactionService = Lazy.async(TransactionService.class);
        this.authenticationService = Lazy.async(AuthenticationService.class);
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService.get();
    }

    @Override
    public AuthenticationService getAuthenticationService() {
        return authenticationService.get();
    }
}
