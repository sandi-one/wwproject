package com.ww.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author sandy
 */
public class JarUtil {

    public static interface ExtractFolderHandler {
        public void extractEntry(String name, boolean isDirectory, InputStream inputStream, long originalSize) throws IOException;
    }

    public static class SaveToFolderHandler implements ExtractFolderHandler {

        private String destFolderPath;
        private java.io.File destFolder;

        public SaveToFolderHandler(String destFolderPath) {
            if (destFolderPath == null) {
                throw new NullPointerException("Destination folder path parameter can not be null");
            }
            this.destFolderPath = destFolderPath;
        }

        @Override
        public void extractEntry(String entryName, boolean isDirectory, InputStream inputStream, long originalSize) throws IOException {
            if (destFolder == null) {
                destFolder = new java.io.File(destFolderPath);
                if (!destFolder.exists()) {
                    destFolder.mkdirs();
                }
            }
            java.io.File entryFile = new java.io.File(destFolder, entryName);
            if (isDirectory && !entryFile.exists()) {
                entryFile.mkdirs();
            } else {
                if (!entryFile.getParentFile().exists()) {
                    entryFile.getParentFile().mkdirs();
                }
                IOUtils.copy(inputStream, new FileOutputStream(entryFile));
            }
        }
    };

    private static class LazyInputStream extends InputStream {

        private final JarFile jarFile;
        private final JarEntry jarEntry;
        private InputStream inputStream;

        public LazyInputStream(JarFile jarFile, JarEntry jarEntry) {
            this.jarFile = jarFile;
            this.jarEntry = jarEntry;
        }

        private void initInputStream() throws IOException {
            if (inputStream == null) {
                inputStream = jarFile.getInputStream(jarEntry);
            }
        }

        @Override
        public int read() throws IOException {
            initInputStream();
            return inputStream.read();
        }

        @Override
        public int read(byte[] bytes) throws IOException {
            initInputStream();
            return inputStream.read(bytes);
        }

        @Override
        public int read(byte[] bytes, int i, int i1) throws IOException {
            initInputStream();
            return inputStream.read(bytes, i, i1);
        }

        @Override
        public void close() throws IOException {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static JarFile getFarFileByFile(File file) throws IOException {
        String path = file.getAbsolutePath();
        return getJarFileByJarEntryPath(path);
    }

    private static JarFile getJarFileByJarEntryPath(String jarEntryPath) throws IOException {
        int srcSkip = 0;
        if (jarEntryPath.startsWith("jar:")) {
            srcSkip += 4;
        }
        if (jarEntryPath.startsWith("file:", srcSkip)) {
            srcSkip += 5;

        }
        String jarFilePath = jarEntryPath;
        int index = jarEntryPath.indexOf("!");
        if (index != -1) {
            jarFilePath = jarEntryPath.substring(srcSkip, index);
        }
        return new JarFile(jarFilePath.replace("%20", " "));
    }

    private static String getEntryPatParthByJarEntryPath(String jarEntryPath) throws IOException {
        return jarEntryPath.substring(jarEntryPath.indexOf("!") + 2);
    }

    public static void extractFolder(JarFile jarFile, String srcFolderPath, ExtractFolderHandler handler)
            throws IOException {
        if (jarFile == null) {
            throw new NullPointerException("Jar file parameter can not be null");
        }
        if (srcFolderPath == null) {
            throw new NullPointerException("Source folder path parameter can not be null");
        }
        if (handler == null) {
            throw new NullPointerException("Handler parameter can not be null");
        }
        if (srcFolderPath.startsWith("/")) {
            srcFolderPath = srcFolderPath.substring(1);
        }
        if (!srcFolderPath.endsWith("/")) {
            srcFolderPath += "/";
        }
        Enumeration<JarEntry> entries = jarFile.entries();
        while(entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().startsWith(srcFolderPath)) {
                if (jarEntry.getName().equals(srcFolderPath)) {
                    // skip base folder
                    continue;
                }
                String entryName = jarEntry.getName().substring(srcFolderPath.length());
                if (jarEntry.isDirectory() && entryName.endsWith("/")) {
                    entryName = entryName.substring(0, entryName.length() - 1);
                }
                handler.extractEntry(entryName, jarEntry.isDirectory(), new LazyInputStream(jarFile, jarEntry), jarEntry.getSize());
            }
        }
    }

    public static void extractFolder(JarFile jarFile, String srcFolderPath, String destFolderPath)
            throws IOException {
        extractFolder(jarFile, srcFolderPath, new SaveToFolderHandler(destFolderPath));
    }

    public static void extractFolder(String srcFolderPath, ExtractFolderHandler handler) throws IOException {
        if (srcFolderPath == null) {
            throw new NullPointerException("Source folder path parameter can not be null");
        }
        JarFile jarFile = getJarFileByJarEntryPath(srcFolderPath);
        String jarEntryPath = getEntryPatParthByJarEntryPath(srcFolderPath);
        extractFolder(jarFile, jarEntryPath, handler);
    }

    /**
     * Extract folder from jar to filesystem
     * @param srcFolderPath Path in format "jar:file:/path/to/jar/file.jar!/path/to/folder/in/jar"
     *  or file:/path/to/jar/file.jar!/path/to/folder/in/jar" or /path/to/jar/file.jar!/path/to/folder/in/jar"
     * @param destFolderPath
     * @throws IOException
     */
    public static void extractFolder(String srcFolderPath, String destFolderPath)
            throws IOException {
        extractFolder(srcFolderPath, new SaveToFolderHandler(destFolderPath));
    }

}
