package com.ww.server.action;

import com.ww.server.DefaultSocketHandler;
import com.ww.server.data.Parameters;
import com.ww.server.data.ResponseMap;
import com.ww.server.data.ResponseRepresentation;
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
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.websocket.WebSocket;

/**
 *
 * @author sandy
 */
public class ActionHandler implements WebSocket.OnTextMessage {

    private static final Logger _log = new Slf4jLog(ActionHandler.class.getName());

    protected WebSocket.Connection connection;

    @Override
    public void onOpen(WebSocket.Connection connection) {
        this.connection = connection;
        DefaultSocketHandler.getWebSockets().add(this);
        if (_log.isDebugEnabled()) {
            _log.debug("Connection open");
        }
    }

    @Override
    public void onMessage(String data) {
        Parameters parameters = new Parameters();
        parameters.setParameters(data);

        try {
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
                        _log.warn("Unable to call " + cls.getName());
                        continue;
                    }
                    // request url and Action annotation should be in lower case!
                    actionId = actionId.toLowerCase();

                    if (parameters.getAction().equals(actionId)) {
                        // if action class found then instance action
                        Constructor<? extends BaseAction> constructor = cls.getConstructor();
                        BaseAction newInstance = constructor.newInstance();
                        newInstance.validate(parameters);
                        newInstance.preProcessAction();
                        ResponseMap response = newInstance.processAction(parameters);
                        // send representation to client
                        connection.sendMessage(ResponseRepresentation.getRepresentation(response));
                        return;
                    }
                    // else continue search
                } catch (Exception e) {
                    _log.warn("Unable to instance action", e);
                }
            }
            // TODO throw BadRequestException
        } catch (Exception e) {
            _log.warn("Actions not found", e);
        }
    }

    @Override
    public void onClose(int closeCode, String message) {
        connection.close();
        DefaultSocketHandler.getWebSockets().remove(this);
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
                _log.warn("ActionHandler class should be located in not-jar file", ex);
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
                _log.warn("Class " + cls.getName() + " cannot be abstract.");
            } else if (!BaseAction.class.isAssignableFrom(cls)) {
                _log.warn("Class " + cls.getName() + " must be a subtype of the BaseAction class.");
            } else {
                return true;
            }
        }
        return false;
    }
}
