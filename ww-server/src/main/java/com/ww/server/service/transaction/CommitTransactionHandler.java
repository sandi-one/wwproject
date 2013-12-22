package com.ww.server.service.transaction;

import com.ww.server.events.Handler;

/**
 *
 * @author sandy
 */
public interface CommitTransactionHandler extends Handler {

    void onCommit();
}
