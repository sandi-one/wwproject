package com.ww.server.service;

import com.ww.server.service.authentication.AuthenticationService;
import com.ww.server.service.transaction.TransactionService;

/**
 *
 * @author sandy
 */
public interface Factory {

    TransactionService getTransactionService();
    AuthenticationService getAuthenticationService();
}
