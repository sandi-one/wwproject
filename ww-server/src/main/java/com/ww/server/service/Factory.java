package com.ww.server.service;

import com.ww.server.service.transaction.TransactionService;

/**
 *
 * @author sandy
 */
public interface Factory {

    TransactionService getTransactionService();
}
