package com.ww.server.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.log.Slf4jLog;

/**
 *
 * @author sandy
 */
public class WWService extends ServiceFactory implements WWFactory {

    private static final Logger _log = new Slf4jLog(WWService.class.getName());

    @Override
    public void init() {
        // invoke accessor methods for all services
        for (Method method : Factory.class.getMethods()) {
            if (method.getParameterTypes().length == 0) {
                try {
                    method.invoke(this, new Object[] {});
                } catch (IllegalAccessException ex) {
                    _log.warn(ex.getMessage(), ex);
                } catch (IllegalArgumentException ex) {
                    _log.warn(ex.getMessage(), ex);
                } catch (InvocationTargetException ex) {
                    _log.warn(ex.getMessage(), ex);
                }
            }
        }
    }
}
