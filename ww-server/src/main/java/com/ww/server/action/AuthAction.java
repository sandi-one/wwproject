package com.ww.server.action;

import com.ww.server.exception.ActionErrors;
import com.ww.server.exception.ActionException;
import com.ww.server.model.Account;
import com.ww.server.service.authentication.AuthenticationService;
import com.ww.server.service.exception.ServiceException;

/**
 *
 * @author sandy
 */
public class AuthAction extends BaseAction {

    protected Account actionAccount;

    protected void tryLogin() throws ActionException {
        AuthenticationService authService = service.getAuthenticationService();
        try {
            try {
                if (tokenId != null) {
                    authService.validateToken(tokenId);
                } else {
                    throw new ActionException(ActionErrors.NOT_AUTHENTICATED);
                }
            } catch (ServiceException ex) {
                throw new ActionException(ActionErrors.NOT_AUTHENTICATED);
            }
        } finally {
            authService.setCurrentAccount(actionAccount);
        }
    }

    @Override
    public void preProcessAction() throws ActionException {
        super.preProcessAction();
        tryLogin();
    }
}
