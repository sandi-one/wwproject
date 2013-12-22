package com.ww.server.util;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.context.internal.ThreadLocalSessionContext;

/**
 *
 * @author sandy
 */
// Use HibernateUtil just in Persistence and QueryUtils
@Deprecated
public class HibernateUtil {

    private static Logger _log = Logger.getLogger(HibernateUtil.class.getName());

    private static HibernateUtil _instance = new HibernateUtil();
    private SessionFactory _sessionFactory;
    private static String contextImplementation;

    public static Session openSession() {
        return _instance.getSessionFactoryInstance().openSession();
    }

    public static void closeSession(Session session) {
        try {
            if (session != null) {
                if (session.isOpen()) {
                    session.close();
                }
            }
        } catch (HibernateException he) {
            _log.log(Level.SEVERE, "closeSession", he);
        }
    }

    /**
     * This method is deprecated. Use TransactionScope.getSessionForScope();
     *
     * @return
     * @throws HibernateException
     * @deprecated
     */
    @Deprecated
    public static Session getSession() throws HibernateException {
        Session currentSession = _instance.getSessionFactoryInstance().getCurrentSession();
        // check that current session is closed
        if (currentSession != null && !currentSession.isOpen()) {
            _log.log(Level.SEVERE, "Wrong session state. Trying to unbind");
            if ("thread".equalsIgnoreCase(contextImplementation)) {
                ThreadLocalSessionContext.unbind(_instance.getSessionFactoryInstance());
                currentSession = _instance.getSessionFactoryInstance().getCurrentSession();
            }
            if ("managed".equalsIgnoreCase(contextImplementation)) {
                ManagedSessionContext.unbind(_instance.getSessionFactoryInstance());
                currentSession = _instance.getSessionFactoryInstance().getCurrentSession();
            }
        }
        return currentSession;
    }

    private SessionFactory getSessionFactoryInstance() {
        if (_sessionFactory == null) {
            Configuration configuration = null;
            try {
                configuration = new Configuration();
                configuration = configuration.configure();
            } catch (Throwable e) {
                _log.log(Level.SEVERE, e.getMessage(), e);
            }
            if (configuration != null) {
                Properties properties = configuration.getProperties();
                contextImplementation = properties.getProperty(
                        Environment.CURRENT_SESSION_CONTEXT_CLASS);

                try {
                    _sessionFactory = configuration.buildSessionFactory();
                } catch (Exception e) {
                    _log.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        return _sessionFactory;
    }

    private HibernateUtil() {
    }
}
