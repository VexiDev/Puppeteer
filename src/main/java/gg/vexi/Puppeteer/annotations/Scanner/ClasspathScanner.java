package gg.vexi.Puppeteer.annotations.Scanner;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathScanner {

    public static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<Class<?>> classes = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("jar")) {
                JarURLConnection jarConn = (JarURLConnection) resource.openConnection();
                classes.addAll(findClassesInJar(jarConn.getJarFile(), path));
            } else {
                classes.addAll(findClasses(new File(resource.getFile()), packageName));
            }
        }

        return classes;
    }

    private static List<Class<?>> findClassesInJar(JarFile jarFile, String path) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.startsWith(path) && entryName.endsWith(".class")) {
                String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                classes.add(Class.forName(className));
            }
        }

        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        
        if (!directory.exists()) {
            return classes;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
        
        return classes;
    }
}
