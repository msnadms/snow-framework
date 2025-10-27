package com.snow.di;

import com.snow.exceptions.ClassScanningException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ComponentScanner {

    private static final Logger logger = Logger.getLogger(ComponentScanner.class.getName());

    public static Set<Class<?>> scan(String basePath)
            throws ClassScanningException {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            var resources = classLoader.getResources(basePath.replace('.', '/'));
            return scanClasses(resources, basePath);
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error while trying to scan classes", e);
            throw new ClassScanningException(basePath, e);
        }
    }

    private static Set<Class<?>> scanClasses(Enumeration<URL> resources, String basePath)
            throws ClassNotFoundException, IOException {
        Set<Class<?>> classes = new HashSet<>();
        var path = basePath.replace('.', '/');
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if (url.getProtocol().equals("file")) {
                findResourcesInDirectory(new File(url.getFile()), basePath, classes);
            } else if (url.getProtocol().equals("jar")) {
                findResourcesInJar(url, path, classes);
            }
        }
        return classes;
    }

    private static void findResourcesInDirectory(File directory, String packageName, Set<Class<?>> classes)
            throws ClassNotFoundException {
        var files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                findResourcesInDirectory(file, packageName + "." + file.getName(), classes);
            } else {
                String className = packageName + '.' + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
    }

    private static void findResourcesInJar(URL resource, String path, Set<Class<?>> classes)
            throws IOException, ClassNotFoundException {
        String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
                    String className = name.replace('/', '.').replace(".class", "");
                    classes.add(Class.forName(className));
                }
            }
        }
    }
}
