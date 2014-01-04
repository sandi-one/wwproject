package com.ww.server.action;

import com.ww.server.data.Parameters;
import com.ww.server.data.ResponseMap;
import com.ww.server.enums.TagName;
import com.ww.server.enums.TransactionIsolation;
import com.ww.server.exception.ActionException;
import com.ww.server.model.Account;
import com.ww.server.persistence.Persistence;
import com.ww.server.service.Instance;
import com.ww.server.service.WWFactory;
import com.ww.server.service.authentication.Token;
import com.ww.server.service.exception.ServiceException;
import com.ww.server.util.ParamUtil;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.WebSocket;

/**
 *
 * @author sandy
 */
public class BaseAction {

    protected static final Logger _log = Logger.getLogger(BaseAction.class.getName());
    protected WWFactory service = Instance.get();
    protected Account actionAccount;
    protected WebSocket.Connection connection = null;
    protected Token token;
    private TransactionIsolation previousIsolationLevel = null;

    public BaseAction() {
    }

    public String getActionId() {
        Action annotation = this.getClass().getAnnotation(Action.class);
        return (null == annotation) ? null : annotation.value();
    }

    public void validate(Parameters parameters) throws ActionException {
        connection = (WebSocket.Connection) ParamUtil.getNotNull(parameters, TagName.CONNECTION.toString());
        token = (Token) parameters.get(TagName.TOKEN.toString());
    }

    public void preProcessAction() throws ActionException {
        beginTransactions();
        actionAccount = service.getAuthenticationService().getCurrentAccount();
    }

    public ResponseMap processAction(Parameters parameters) throws ActionException {
        return null;
    }

    public void postProcessAction() throws ActionException {
        commitTransactions();
    }

    public void finalProcessAction() {
        Instance.remove();
    }

    public void postException() {
        rollbackTransactions();
    }

    protected void beginTransactions() throws ActionException {
        try {
            service.beginTransaction();
            if (getTxIsolation() != null) {
                previousIsolationLevel = TransactionIsolation.valueOf(Persistence.getSystemVariable("tx_isolation").replace('-', '_'));
                setIsolationLevel(getTxIsolation());
            }
        } catch (ServiceException e) {
            _log.log(Level.SEVERE, "beginTransactions", e);
            throw e;
        }
    }

    private void setIsolationLevel(TransactionIsolation isolationLevel) {
        if (isolationLevel == null) {
            return;
        }
        Map<String, Object> param = Collections.singletonMap("txIsolation", (Object) isolationLevel.toString());
        Persistence.executeSQLUpdate("SET SESSION tx_isolation = :txIsolation", param);
    }

    protected TransactionIsolation getTxIsolation() {
        return null;
    }

    protected void commitTransactions() throws ActionException {
        try {
            setIsolationLevel(previousIsolationLevel);
            service.commit();
        } catch (ServiceException e) {
            _log.log(Level.SEVERE, "commitTransactions", e);
            // TODO: Set proper error message.
            throw e;
        }
    }

    @SuppressWarnings("deprecation")
    private void rollbackTransactions() {
        try {
            setIsolationLevel(previousIsolationLevel);
            service.rollback();
        } catch (ServiceException ex) {
            // Ignore but write to log for analize
            _log.log(Level.SEVERE, "rollbackTransactions", ex);
        }
    }
}
