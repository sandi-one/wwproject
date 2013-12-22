package com.ww.server.persistence;

import com.ww.server.persistence.exception.DeleteException;
import com.ww.server.persistence.exception.FetchByIdException;
import com.ww.server.persistence.exception.FetchManyException;
import com.ww.server.persistence.exception.PersistenceException;
import com.ww.server.persistence.exception.PureSqlQueryException;
import com.ww.server.persistence.exception.SaveException;
import com.ww.server.persistence.exception.UnexpectedException;
import com.ww.server.persistence.exception.UniquenessException;
import com.ww.server.persistence.exception.UpdateException;
import com.ww.server.util.HibernateUtil;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;

/**
 *
 * @author sandy
 */
public class Persistence {
    private static final Logger _log = Logger.getLogger(Persistence.class.getName());

    public static final boolean HQL_QUERY = true;
    public static final boolean SQL_QUERY = false;
    public static final Integer NON_LIMIT = null;
    public static final Class MIXED_ENTITY = Object[].class;

    /**
     * Implement the interface in order to work with hibernate session. Is used
     * in Persistence.execHibernate() function.
     *
     * @param <T>
     */
    protected static abstract class HibernateExecutor<T> {

        // Handler.
        public abstract T process(Session session) throws RuntimeException;

        // Override the method to customize exception handling.
        public RuntimeException processException(HibernateException e) throws RuntimeException {
            return null;
        }
    }

    public static abstract class UpdateExecutor extends HibernateExecutor<Integer> {

        private Query q;

        public abstract Query makeQuery(Session session);

        @Override
        public Integer process(Session session) {
            q = makeQuery(session);
            return q.executeUpdate();
        }

        @Override
        public PersistenceException processException(HibernateException e) {
            return new PureSqlQueryException(q == null ? null : q.getQueryString(), e);
        }
    }

    public static abstract class ListExecutor extends HibernateExecutor<List> {

        private Query q;
        private final Class entityClass;

        public ListExecutor(Class entityClass) {
            this.entityClass = entityClass;
        }

        public abstract Query makeQuery(Session session);

        @Override
        public List process(Session session) {
            q = makeQuery(session);
            if (entityClass != null && !entityClass.equals(MIXED_ENTITY)
                    && q instanceof SQLQuery) {
                ((SQLQuery) q).addEntity(entityClass);
            }
            return q.list();
        }

        @Override
        public PersistenceException processException(HibernateException e) {
            return new FetchManyException(entityClass, e);
        }
    }

    public static abstract class SingleResultExecutor<T> extends HibernateExecutor<T> {

        private final Class entityClass;

        public SingleResultExecutor(Class<T> entityClass) {
            this.entityClass = entityClass;
        }

        public abstract Query makeQuery(Session session);

        @Override
        public T process(Session session) {
            Query q = makeQuery(session);
            return (T) q.uniqueResult();
        }

        @Override
        public PersistenceException processException(HibernateException e) {
            if (e instanceof NonUniqueResultException) {
                return new com.ww.server.persistence.exception.NonUniqueResultException(entityClass, e);
            } else {
                return new FetchManyException(entityClass, e);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static <T> T execHibernate(final HibernateExecutor<T> executor) throws PersistenceException {
        try {
            return TransactionScope.runInScope(new TransactionScope.Command<T>() {

                @Override
                public T executeInScope(Session session) throws Exception {

                    T res = executor.process(session);

                    return res;
                }
            });

        } catch (HibernateException e) {
            RuntimeException pe = executor.processException(e);
            if (null == pe) {
                pe = new UnexpectedException(e);
            }
            if (!TransactionScope.checkExceptionToRestartTransaction(e)) {
                _log.log(Level.FINE, "", pe);
            }
            throw pe;
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                // At this moment only Hibernate or RuntimeException can be thrown
                // if it will be changed, other exceptions should be handled here
                throw new RuntimeException("Unexpected exception", ex);
            }
        }

    }

    /**
     * Updates the entity which already exsists in the database
     *
     * @param entity entity to update
     * @throws PersistenceException if updating fails
     */
    public static void update(final Object entity) throws PersistenceException {

        execHibernate(new HibernateExecutor<Void>() {

            @Override
            public Void process(Session session) throws PersistenceException {
                session.update(entity);
                session.flush();
                return null;
            }

            @Override
            public PersistenceException processException(HibernateException e) {
                if (e instanceof ConstraintViolationException) {
                    return new UniquenessException(entity.getClass(), e);
                } else {
                    return new UpdateException(entity.getClass(), e);
                }
            }
        });

    }

    /**
     * Saves new entity to the database
     *
     * @param entity entity to save
     * @throws PersistenceException if saving fails
     */
    public static void save(final Object entity)
            throws PersistenceException {

        execHibernate(
                new HibernateExecutor<Void>() {

                    @Override
                    public Void process(Session session) {
                        session.save(entity);
                        try {
                            session.flush();
                        } catch (ConstraintViolationException ex) {
                            // After update hibernate version in TCCTWO-23311 we've found a problem
                            // with NotUnique entities. If first attempt was failed - than change some fields
                            // in saved entity - than try to save. In this case Hibernate throws exception
                            // when it tries to resave old entry (which cached in some internal queue).
                            // We need to clear it.
                            session.clear();
                            throw ex;
                        }
                        return null;
                    }

                    @Override
                    public PersistenceException processException(HibernateException e) {
                        if (e instanceof ConstraintViolationException) {
                            return new UniquenessException(entity.getClass(), e);
                        } else {
                            return new SaveException(entity.getClass(), e);
                        }
                    }
                });
    }

    /**
     * Deletes specified entity from database
     *
     * @param entity entity to delete
     * @throws PersistenceException if deleting fails
     */
    public static void delete(final Object entity) throws PersistenceException {

        execHibernate(new HibernateExecutor<Void>() {

            @Override
            public Void process(Session session) {
                session.delete(entity);
                session.flush();
                return null;
            }

            @Override
            public PersistenceException processException(HibernateException e) {
                return new DeleteException(entity.getClass(), e);
            }
        });
    }

    /**
     * Refresh specified entity from database
     *
     * @param entity entity to refresh
     */
    @SuppressWarnings("deprecation")
    public static void refresh(Object entity) {
        if (entity == null) {
            return;
        }
        try {
            TransactionScope.getSessionForScope().refresh(entity);
        } catch (Exception ex) {
            _log.log(Level.SEVERE, "Error during entity refreshing", ex);
        }
    }

    @SuppressWarnings("deprecation")
    public static <T> T reload(T entity) {
        Session session = HibernateUtil.getSession();
        Class entityClass = entity.getClass();
        // entity can be Proxy of hibernate model's class, look for model's class
        ClassMetadata classMetadata = null;
        while (classMetadata == null && !entityClass.equals(Object.class)) {
            classMetadata = session.getSessionFactory().getClassMetadata(entityClass.getName());
            if (classMetadata == null) {
                entityClass = entityClass.getSuperclass();
            }
        }
        if (classMetadata == null) {
            return entity;
        }

        String entryId = (String) classMetadata.getIdentifier(entity, (SessionImplementor) session);
        boolean attached = false;
        if (session.contains(entity)) {
            session.evict(entity);
            attached = true;
        }
        T newObject = (T) fetchById(entityClass, entryId);
        if (newObject == null && !attached) {
            newObject = entity;
        }
        return newObject;
    }

    @SuppressWarnings("deprecation")
    public static void clearSession() {
        Session session = HibernateUtil.getSession();
        session.clear();
    }

    /**
     * Deletes entity by its id
     *
     * @param entityClass entity class
     * @param entityId entity id
     * @throws PersistenceException
     */
    public static void deleteById(final Class entityClass, final String entityId)
            throws PersistenceException {

        execHibernate(new HibernateExecutor<Void>() {

            @Override
            public Void process(Session session) {
                Object entity = session.load(entityClass, entityId);
                session.delete(entity);
                session.flush();
                return null;
            }

            @Override
            public PersistenceException processException(HibernateException e) {
                return new DeleteException(entityClass, e);
            }
        });
    }

    /**
     * Execute HQL query.
     */
    public static int executeUpdate(String query, String[] fieldNames,
            Object[] fieldValues) throws PersistenceException {
        return executeUpdate(query, fieldNames, fieldValues, HQL_QUERY);
    }

    public static int executeUpdate(String query, Map<String, Object> keys) throws PersistenceException {
        return executeUpdate(query, keys, HQL_QUERY);
    }

    /**
     * Execute HQL/SQL query.
     */
    public static int executeUpdate(final String query, final String[] fieldNames,
            final Object[] fieldValues, final boolean isHQL) throws PersistenceException {

        UpdateExecutor executor = new UpdateExecutor() {

            @Override
            public Query makeQuery(Session session) {
                return QueryUtils.makeQuery(query, fieldNames, fieldValues, NON_LIMIT, isHQL);
            }
        };

        return execHibernate(executor);
    }

    public static int executeUpdate(final String query, final Map<String, Object> keys,
            final boolean isHQL) throws PersistenceException {

        UpdateExecutor executor = new UpdateExecutor() {

            @Override
            public Query makeQuery(Session session) {
                return QueryUtils.makeQuery(query, keys, NON_LIMIT, isHQL);
            }
        };

        return execHibernate(executor);
    }

    public static int executeSQLUpdate(final String query, final Map<String, Object> keys) {
        return executeUpdate(query, keys, SQL_QUERY);
    }

    /**
     * Execute SQL query.
     */
    public static int executeSQLQuery(String query) throws PersistenceException {
        return executeUpdate(query, null, null, SQL_QUERY);
    }

    /**
     * Use it only with DELETE statements without LIMIT parameter
     */
    public static void executeSqlDelete(String query, Map<String, Object> keys, final Integer loopLimit, boolean commit) throws PersistenceException {
        if (loopLimit == NON_LIMIT) {
            executeSQLUpdate(query, keys);
        } else {
            query += " LIMIT " + loopLimit;
            int count;
            do {
                count = executeSQLUpdate(query, keys);
                if (commit) {
                    executeSQLQuery("commit");
                }
            } while (loopLimit == count);
        }
    }

    public static void executeSqlDelete(String query, Map<String, Object> keys, final Integer loopLimit) throws PersistenceException {
        executeSqlDelete(query, keys, loopLimit, false);
    }

    /**
     * Returns entity by its id
     *
     * @param entityClass entity class
     * @param entityId entity id
     * @return entity
     * @throws PersistenceException
     */
    public static <T> T fetchById(final Class<T> entityClass, final String entityId)
            throws PersistenceException {

        return execHibernate(new HibernateExecutor<T>() {

            @Override
            public T process(Session session) {
                try {
                    Object obj = session.get(entityClass, entityId);
                    if (obj instanceof HibernateProxy) {
                        return (T) ((HibernateProxy) obj).getHibernateLazyInitializer().getImplementation();
                    } else {
                        return (T) obj;
                    }
                } catch (ObjectNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public PersistenceException processException(HibernateException e) {
                return new FetchByIdException(entityClass, entityId, e);
            }
        });
    }

    public static <T> List<T> fetchByIds(final Class<T> entityClass,
            final Collection<String> entryIds) throws PersistenceException {
        if (entryIds.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return execHibernate(new HibernateExecutor<List<T>>() {

            @Override
            public List<T> process(Session session) throws RuntimeException {
                String idName = session.getSessionFactory().
                        getClassMetadata(entityClass.getName()).
                        getIdentifierPropertyName();

                String query = "FROM " + entityClass.getSimpleName() + " t\n"
                        + " WHERE t." + idName + " IN (:entryIds)";

                return fetchListByQueryWithParams(entityClass, query,
                        "entryIds", entryIds);
            }

            @Override
            public RuntimeException processException(HibernateException e) {
                return new FetchManyException(entityClass, e);
            }
        });
    }

    /**
     * Returns list of entries by HQL query and named params.
     */
    public static List fetchListByQueryWithParams(final Class entityClass, final String query,
            final Map<String, Object> params) throws PersistenceException {
        return fetchListByQueryWithParams(entityClass, query, params, NON_LIMIT, HQL_QUERY);
    }

    public static List fetchListByQueryWithParams(final Class entityClass, final String query,
            String fieldName, Object fieldValue) throws PersistenceException {
        return fetchListByQueryWithParams(entityClass, query,
                new String[]{fieldName}, new Object[]{fieldValue}, NON_LIMIT, HQL_QUERY);
    }

    public static List fetchListByQueryWithParams(final Class entityClass, final String query,
            String[] fieldNames, Object[] fieldValues) throws PersistenceException {
        return fetchListByQueryWithParams(entityClass, query, fieldNames, fieldValues, NON_LIMIT,
                HQL_QUERY);
    }

    /**
     * Returns limited list of entries as result of in query.
     *
     * @param entityClass entity class
     * @param query SQL or HQL with query
     * @param params values of named params
     * @param limit max number of return entries
     * @return
     * @throws PersistenceException
     */
    public static <T> List<T> fetchListByQueryWithParams(final Class<T> entityClass, final String query,
            final Map<String, Object> params, final Integer limit, final boolean isHQL) throws PersistenceException {

        ListExecutor executor = new ListExecutor(entityClass) {

            @Override
            public Query makeQuery(Session session) {
                return QueryUtils.makeQuery(query, params, limit, isHQL);
            }
        };

        return execHibernate(executor);
    }

    public static List fetchListBySQLQuery(String query, Map<String, Object> params) {
        return fetchListByQueryWithParams(MIXED_ENTITY, query, params, NON_LIMIT, SQL_QUERY);
    }

    public static List fetchListByQueryWithParams(final Class entityClass, final String query,
            final Map<String, Object> params, final Integer limit, final int offset,
            final boolean isHQL) throws PersistenceException {

        ListExecutor executor = new ListExecutor(entityClass) {

            @Override
            public Query makeQuery(Session session) {
                return QueryUtils.makeQuery(query, params, limit, offset, isHQL);
            }
        };

        return execHibernate(executor);
    }

    /**
     * Returns limited list of entries as result of in query.
     *
     * @param entityClass entity class
     * @param query SQL or HQL with query
     * @param params values of named params
     * @param limit max number of return entries
     * @return
     * @throws PersistenceException
     */
    public static List fetchListByQueryWithParams(final Class entityClass, final String query,
            final String[] fieldNames, final Object[] fieldValues, final Integer limit,
            final boolean isHQL) throws PersistenceException {

        ListExecutor executor = new ListExecutor(entityClass) {

            @Override
            public Query makeQuery(Session session) {
                return QueryUtils.makeQuery(query, fieldNames, fieldValues, limit, isHQL);
            }
        };

        return execHibernate(executor);
    }

    public static int fetchSingleIntByQueryWithParams(final String query, final Map<String, Object> params)
            throws PersistenceException {
        return fetchSingleByQueryWithParams(BigInteger.class, query, params, false).intValue();
    }

    /**
     * Returns single result on HQL query.
     */
    public static <T> T fetchSingleByQueryWithParams(final Class<T> entityClass,
            final String query, String[] fieldNames, Object[] fieldValues) throws PersistenceException {

        return fetchSingleByQueryWithParams(entityClass, query, fieldNames, fieldValues, HQL_QUERY);
    }

    public static <T> T fetchSingleByQueryWithParams(final Class<T> entityClass,
            final String query, String fieldNames, Object fieldValues) throws PersistenceException {

        return fetchSingleByQueryWithParams(entityClass, query, new String[]{fieldNames},
                new Object[]{fieldValues}, HQL_QUERY);
    }

    public static <T> T fetchSingleByQueryWithParams(final Class<T> entityClass,
            final String query, final Map<String, Object> params) throws PersistenceException {

        return fetchSingleByQueryWithParams(entityClass, query, params, HQL_QUERY);
    }

    public static <T> T fetchSingleByQueryWithParams(final Class<T> entityClass,
            final String query, final Map<String, Object> params, final boolean isHQL) throws PersistenceException {

        SingleResultExecutor executor = new SingleResultExecutor(entityClass) {

            @Override
            public Query makeQuery(Session session) {
                return QueryUtils.makeQuery(query, params, NON_LIMIT, isHQL);
            }
        };

        return (T) execHibernate(executor);
    }

    /**
     * Returns single result on SQL/HQL query.
     */
    public static <T> T fetchSingleByQueryWithParams(final Class<T> entityClass,
            final String query, final String[] fieldNames, final Object[] fieldValues,
            final boolean isHQL) throws PersistenceException {

        HibernateExecutor<T> executor = new SingleResultExecutor(entityClass) {

            @Override
            public Query makeQuery(Session session) {
                return QueryUtils.makeQuery(query, fieldNames, fieldValues, NON_LIMIT, isHQL);
            }
        };

        return execHibernate(executor);
    }

    /**
     * Find entity by example
     *
     * @param entityClass entityClass entity class
     * @param exampleFields Example fields for search, i.e. search criteria.
     * @return
     * @throws PersistenceException
     */
    protected static <T> T fetchSingleByExample(Class<T> entityClass, Map<String, ? extends Object> exampleFields)
            throws PersistenceException {
        return fetchSingleByExample(entityClass, exampleFields.keySet().toArray(new String[0]),
                exampleFields.values().toArray());
    }

    protected static <T> T fetchSingleByExample(final Class<T> entityClass, String[] fieldNames, Object[] fieldvalues)
            throws PersistenceException {

        // todo: check, that fieldNames.length = fieldvalues.length
        // todo: check fieldNames != null, fieldvalues != null;
        checkInExampleParams(fieldNames, fieldvalues);

        final DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);
        if ((fieldNames != null) && (fieldvalues != null)
                && (fieldvalues.length == fieldNames.length)) {
            for (int i = 0; i < fieldNames.length; ++i) {
                criteria.add(Property.forName(fieldNames[i]).eq(fieldvalues[i]));
            }
        }

        return execHibernate(
                new HibernateExecutor<T>() {

                    @Override
                    public T process(Session session) {
                        return (T) criteria.getExecutableCriteria(session).uniqueResult();
                    }

                    @Override
                    public PersistenceException processException(HibernateException e) {
                        if (e instanceof NonUniqueResultException) {
                            return new com.ww.server.persistence.exception.NonUniqueResultException(
                                    entityClass, e);
                        } else {
                            return new FetchManyException(entityClass, e);
                        }
                    }
                });
    }

    /**
     * Find list of entries by example (with help of DetachedCriteria)
     * ATTENTION: the fetchListByExample2 throws exception (because of hibernate
     * bug http://opensource.atlassian.com/projects/hibernate/browse/HHH-1570)
     * in case of referenced selection based on <key-many-to-one>, i.e. when
     * such fields as "device.devieId" are used in WHERE construction and the
     * field "device" is <key-many-to-one>. Example, where it can't be used:
     * MountPointConnection.device
     *
     * @param entityClass
     * @param keys
     * @param orderByConditions
     * @return
     * @throws PersistenceException
     */
    protected static List fetchListByExample2(Class entityClass, Map<String, Object> keys,
            LinkedHashMap<String, OrderDirection> orderByConditions) throws PersistenceException {

        if (null == keys) {
            return fetchListByExample(entityClass, null, null, orderByConditions);
        }

        final DetachedCriteria criteria = QueryUtils.makeCriteria(entityClass, keys);

        if (null != orderByConditions) {
            for (Map.Entry<String, OrderDirection> order : orderByConditions.entrySet()) {
                switch (order.getValue()) {
                    case ASC: {
                        criteria.addOrder(Order.asc(order.getKey()));
                        break;
                    }

                    case DESC: {
                        criteria.addOrder(Order.desc(order.getKey()));
                        break;
                    }
                }
            }
        }

        return execHibernate(new HibernateExecutor<List>() {

            @Override
            public List process(Session session) {
                return criteria.getExecutableCriteria(session).list();
            }
        });
    }

    protected static <T> List<T> fetchListByExample(final Class<T> entityClass, final Map<String, Object> keys,
            final LinkedHashMap<String, OrderDirection> orderByConditions) throws PersistenceException {

        return execHibernate(new HibernateExecutor<List<T>>() {
            @Override
            public List<T> process(Session session) {
                Query query = QueryUtils.buildQueryByKeys(session,
                        entityClass, keys, orderByConditions);

                return (List<T>) query.list();
            }
        });
    }

    protected static <T> List<T> fetchListByExample(Class<T> entityClass, Map<String, Object> keys) throws PersistenceException {
        return fetchListByExample(entityClass, keys, null);
    }

    protected static <T> List<T> fetchListByExample(final Class<T> entityClass,
            final String[] fieldNames, final Object[] fieldvalues,
            final LinkedHashMap<String, OrderDirection> orderByConditions)
            throws PersistenceException {

        checkInExampleParams(fieldNames, fieldvalues);

        Map<String, Object> keys = new HashMap<String, Object>();
        for (int i = 0; i < fieldNames.length; ++i) {
            keys.put(fieldNames[i], fieldvalues[i]);
        }

        return fetchListByExample(entityClass, keys);
    }

    private static void checkInExampleParams(String[] fieldNames, Object[] fieldvalues) {
        if ((fieldNames != null) && (fieldvalues != null)
                && (fieldvalues.length != fieldNames.length)) {
            // todo: throw exception
        }
    }

    /**
     * Returns total entity count
     *
     * @param entity entity to count
     * @return total count
     * @throws PersistenceException
     */
    protected static long countAll(Class entity) throws PersistenceException {
        return countAllForOrg(entity, null);
    }

    public static long countAllForOrg(final Class entity, final String orgId) throws PersistenceException {
        String query = "SELECT COUNT(*) FROM " + entity.getSimpleName();
        if (!orgId.isEmpty()) {
            query += " WHERE orgId = :orgid";
        }
        Map<String, Object> params = new HashMap<String, Object>();
        if (!orgId.isEmpty()) {
            params.put("orgid", orgId);
        }

        Long res = (Long) fetchSingleByQueryWithParams(entity, query, params);
        return res.longValue();
    }

    public static <T> List<T> fetchListAll(Class<T> entityClass, LinkedHashMap<String, OrderDirection> orderByConditions)
            throws PersistenceException {

        HSQLBuilder builder = new HSQLBuilder(entityClass);
        builder.orderBy(orderByConditions);
        final String query = builder.toString();

        return fetchListByQueryWithParams(entityClass, query, null);
    }

    /**
     * General interface when we need an universal way to fetch by rows from raw
     * SQL query
     */
    public interface IRowFetcher {

        // return false to stop enumeration
        public boolean process(Object[] row);
    }

    /**
     * Common code to enumerate SQL results using by individual rows
     *
     * @param q
     * @param fetcher
     */
    protected static void processSQLQueryWithRowFetcher(Query q, IRowFetcher fetcher) {
        ScrollableResults iter = q.scroll(ScrollMode.FORWARD_ONLY);
        processSQLQueryWithRowFetcher(iter, fetcher);
    }

    protected static void processSQLQueryWithRowFetcher(ScrollableResults iter, IRowFetcher fetcher) {
        if (iter.first()) {
            do {
                Object[] row = iter.get();

                if (!fetcher.process(row)) {
                    break;
                }
            } while (iter.next());
        }

        iter.close();
    }

    public static boolean doesTableExist(String table) {
        List list = Persistence.fetchListBySQLQuery("SHOW TABLES FROM auditlog LIKE :table",
                Collections.singletonMap("table", (Object) table));
        return !list.isEmpty();
    }

    public static String getSystemVariable(String name) {
        String query = "SHOW VARIABLES LIKE :name";
        Map param = Collections.singletonMap("name", name);
        Object[] row = (Object[]) fetchSingleByQueryWithParams(MIXED_ENTITY, query, param, SQL_QUERY);
        return row == null ? null : row[1].toString();
    }

    public static int getSystemVariableAsInt(String name) {
        return Integer.valueOf(getSystemVariable(name));
    }

    public static String setSystemVariable(String name, String value) {
        String oldValue = getSystemVariable(name);
        Map<String, Object> param = Collections.singletonMap("value", (Object) value);
        executeSQLUpdate(String.format("SET SESSION %s = :value", name), param);
        return oldValue;
    }
}
