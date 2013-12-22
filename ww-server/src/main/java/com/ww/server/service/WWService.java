package com.ww.server.service;

import com.ww.server.service.exception.ServiceException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sandy
 */
public class WWService extends ServiceFactory implements WWFactory {

    private static final Logger _log = Logger.getLogger(WWService.class.getName());

    @Override
    public void init() {
        // invoke accessor methods for all services
        for (Method method : Factory.class.getMethods()) {
            if (method.getParameterTypes().length == 0) {
                try {
                    method.invoke(this, new Object[] {});
                } catch (IllegalAccessException ex) {
                    _log.log(Level.WARNING, ex.getMessage(), ex);
                } catch (IllegalArgumentException ex) {
                    _log.log(Level.WARNING, ex.getMessage(), ex);
                } catch (InvocationTargetException ex) {
                    _log.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
    }

    public void beginTransaction() throws ServiceException {
        getTransactionService().beginTransaction();
    }

    public void commit() throws ServiceException {
        getTransactionService().commit();
    }

    public void rollback() throws ServiceException {
        getTransactionService().rollback();
    }
}
