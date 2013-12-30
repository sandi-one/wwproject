package com.ww.server;

import com.ww.server.action.Action;
import com.ww.server.action.BaseAction;
import com.ww.server.data.Parameters;
import com.ww.server.data.ResponseMap;
import com.ww.server.data.ResponseRepresentation;
import com.ww.server.enums.TagName;
import com.ww.server.exception.ActionErrors;
import com.ww.server.exception.ActionException;
import com.ww.server.exception.RuntimeWrapperException;
import com.ww.server.persistence.InnodbDeadlockRetrier;
import com.ww.server.service.Factory;
import com.ww.server.service.Instance;
import com.ww.server.service.WWFactory;
import com.ww.server.service.WWService;
import com.ww.server.service.authentication.AuthenticationService;
import com.ww.server.service.authentication.TokenManager;
import com.ww.server.util.JarUtil;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.WebSocket;

/**
 *
 * @author sandy
 */
public class ActionHandler implements WebSocket.OnTextMessage {

    private static final Logger _log = Logger.getLogger(ActionHandler.class.getName());
    protected WebSocket.Connection connection;

    @Override
    public void onOpen(WebSocket.Connection connection) {
        this.connection = connection;
        SocketHandler.getWebSockets().add(this);

        _log.fine("Connection open");
    }

    @Override
    @SuppressWarnings({"UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch"})
    public void onMessage(String data) {
        try {
            final Parameters parameters = new Parameters();
            parameters.setParameters(data);
            parameters.put(TagName.CONNECTION.toString(), this.connection);

            String packageName = BaseAction.class.getPackage().getName();
            List<Class<? extends BaseAction>> classes = getActionClasses(packageName);
            // searching required action in com.ww.server.action package
            for (Class<? extends BaseAction> cls : classes) {
                try {
                    String actionId = null;
                    Action annotation = cls.getAnnotation(Action.class);
                    if (annotation != null) {
                        actionId = annotation.value();
                    }
                    if (actionId == null || actionId.trim().equals("")) {
                        actionId = cls.newInstance().getActionId();
                    }
                    if (actionId == null || actionId.trim().equals("")) {
                        _log.log(Level.SEVERE, "Unable to call {0}", cls.getName());
                        continue;
                    }
                    // request url and Action annotation should be in lower case!
                    actionId = actionId.toLowerCase();

                    if (parameters.getAction().equals(actionId)) {
                        // if action class found then instance action
                        Constructor<? extends BaseAction> constructor = cls.getConstructor();
                        final BaseAction newInstance = constructor.newInstance();
                        final ResponseMap[] mapContainer = new ResponseMap[]{null};
                        InnodbDeadlockRetrier.Command command = new InnodbDeadlockRetrier.Command() {

                            public void execute() throws ActionException {
                                newInstance.validate(parameters);
                                newInstance.preProcessAction();
                                mapContainer[0] = newInstance.processAction(parameters);
                                newInstance.postProcessAction();
                            }

                            public void handleError() throws ActionException {
                                newInstance.postException();
                            }
                        };
                        try {
                            InnodbDeadlockRetrier.executeCommand(command);
                            // send representation to client
                            connection.sendMessage(ResponseRepresentation.getRepresentation(mapContainer[0], true));
                            return;
                        } catch (Throwable thr) {
                            // sending actions' exception's representation to client
                            connection.sendMessage(new DefaultExceptionHandler().handleException(thr));
                            return; // should return from method to avoid Bad Request exception
                        } finally {
                            newInstance.finalProcessAction();
                        }
                    }
                    // else continue search
                } catch (Throwable thr) {
                    // sending all exception's representation to client
                    connection.sendMessage(new DefaultExceptionHandler().handleException(thr));
                    return;
                }
            }

            throw new ActionException(ActionErrors.BAD_REQUEST);
        } catch (ActionException e) {
            try {
                connection.sendMessage(new DefaultExceptionHandler().handleException(e));
            } catch (IOException ex) {
                _log.log(Level.SEVERE, "Can't send message", ex);
            }
        } catch (Exception e) {
            _log.log(Level.SEVERE, "Actions not found", e);
        }
    }

    @Override
    public void onClose(int closeCode, String message) {
        connection.close();
        SocketHandler.getWebSockets().remove(this);
        invalidateSession();
    }

    private static void invalidateSession() {
        WWFactory service = Instance.get();
        AuthenticationService authService = service.getAuthenticationService();
        authService.invalidateSession(new TokenManager().getCurrentToken().getToken());
    }

    private static List<Class<? extends BaseAction>> getActionClasses(String packageName)
            throws ClassNotFoundException, IOException {
        File source = new File(BaseAction.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        List<Class<? extends BaseAction>> classes = new LinkedList<Class<? extends BaseAction>>();
        if (source.isDirectory()) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            assert classLoader != null;
            String path = packageName.replace('.', '/');
            List<File> dirs = new ArrayList<File>();
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            for (File directory : dirs) {
                classes.addAll(findActionClasses(directory, packageName));
            }
        } else {
            try {
                JarFile jarFile = JarUtil.getFarFileByFile(source);
                classes = findActionClassesInJar(jarFile, packageName);
            } catch (IOException ex) {
                _log.log(Level.SEVERE, "ActionHandler class should be located in not-jar file", ex);
            }
        }
        return classes;
    }

    private static List<Class<? extends BaseAction>> findActionClasses(File directory, String packageName)
            throws ClassNotFoundException {

        List<Class<? extends BaseAction>> classes = new ArrayList<Class<? extends BaseAction>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findActionClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                Class<?> cls = Class.forName(
                        packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (isActionClass(cls)) {
                    classes.add((Class<? extends BaseAction>) cls);
                }
            }
        }

        return classes;
    }

    private static List<Class<? extends BaseAction>> findActionClassesInJar(JarFile jarFile, String packageName) {
        String jarPackageName = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<JarEntry> entries = jarFile.entries();
        List<Class<? extends BaseAction>> classes = new LinkedList<Class<? extends BaseAction>>();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (!jarEntry.isDirectory()) {
                if (jarEntry.getName().startsWith(jarPackageName)) {
                    int extIndex = jarEntry.getName().lastIndexOf(".class");
                    if (extIndex >= 0 && extIndex + 6 == jarEntry.getName().length()) {
                        String className = jarEntry.getName().replace('/', '.').substring(0, extIndex);
                        try {
                            Class cls = classLoader.loadClass(className);
                            if (isActionClass(cls)) {
                                classes.add(cls);
                            }
                        } catch (ClassNotFoundException ex) {
                            // it is not accessible class
                        } catch (SecurityException ex) {
                            // it is not accessible class
                        }
                    }
                }
            }
        }
        return classes;
    }

    private static boolean isActionClass(Class cls) {
        if (cls.isAnnotationPresent(Action.class)) {
            if (Modifier.isAbstract(cls.getModifiers())) {
                _log.log(Level.SEVERE, "Class {0} cannot be abstract.", cls.getName());
            } else if (!BaseAction.class.isAssignableFrom(cls)) {
                _log.log(Level.SEVERE, "Class {0} must be a subtype of the BaseAction class.", cls.getName());
            } else {
                return true;
            }
        }
        return false;
    }

    private class DefaultExceptionHandler implements ExceptionHandler<String> {

        public String handleException(Throwable ex) {
            if (ex instanceof RuntimeWrapperException) {
                ex = ((RuntimeWrapperException) ex).wrapedException;
            }
            if (ex instanceof ActionException) {
                ActionException ae = (ActionException) ex;
                if (ae.isServerSideError()) {
                    _log.log(Level.SEVERE, ex.getMessage(), ex);
                } else {
                    if (_log.isLoggable(Level.FINE)) {
                        _log.fine(ex.getMessage());
                    }
                }

                return ResponseRepresentation.handleError(ae);
            }
            return ResponseRepresentation.handleError(ex);
        }

    }
}
