package com.tendio.kdt.executor.actions.model;

import com.google.common.collect.Lists;
import com.tendio.kdt.TestProperties;
import com.tendio.kdt.executor.actions.annotation.ActionClass;
import com.tendio.kdt.executor.actions.annotation.ActionDefinition;
import com.tendio.kdt.executor.actions.doc.DocGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ActionsRegistrator {
    private ActionsRegistrator() {
    }

    private static final String DEFAULT_ACTIONS_PACKAGE_NAME = TestProperties.getProperty("test.actions.package");
    private static final Logger LOGGER = LogManager.getLogger();

    public static String getDefaultActionsPackageName() {
        return DEFAULT_ACTIONS_PACKAGE_NAME;
    }

    public static void registerActions() throws IOException, ClassNotFoundException {
        List<Method> annotatedMethods = getAnnotatedActionMethodsForPackage();
        for (Method method : annotatedMethods) {
            String mappingDefinition = method.getAnnotation(ActionDefinition.class).value();
            ActionMethod actionMethod = new ActionMethod(mappingDefinition, method);
            ActionFactory.getRegistry().add(actionMethod);
            LOGGER.debug("Action Method is added to registry: {} ", actionMethod.getMappingDefinition());
        }

        DocGenerator.generateForActionMethods(ActionFactory.getRegistry());
    }

    public static List<Class> findClassesInJar(String jarName, String packageName) throws IOException, ClassNotFoundException {
        ArrayList<String> classesNames = Lists.newArrayList();

        try (JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName))) {
            JarEntry jarEntry;
            //infinite loop protection
            int i = 0;
            //suppose we might have maximum of 5000 jar entries
            final int maxLoopsCount = 5000;
            while ((jarEntry = jarFile.getNextJarEntry()) != null) {
                String name = jarEntry.getName().replaceAll("/", "\\.");
                if ((name.startsWith(packageName)) && name.endsWith(".class")) {
                    classesNames.add(name.replaceAll(".class", ""));
                }
                if (++i > maxLoopsCount) {
                    break;
                }
            }
        }

        ArrayList<Class> classes = Lists.newArrayList();
        for (String className : classesNames) {
            classes.addAll(Collections.singleton(Class.forName(className)));
        }

        LOGGER.debug("Found classes: {} in jar file {}", classes, jarName);
        return classes;
    }

    public static List<Class> findClassesInDirectory(File directory, String packageName) throws ClassNotFoundException {
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        List<Class> classes = Lists.newArrayList();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClassesInDirectory(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }


        LOGGER.debug("Found classes: {} in directory {}", classes, directory.getName());
        return classes;
    }

    private static List<Method> getAnnotatedActionMethodsForPackage() throws IOException, ClassNotFoundException {
        List<Method> methods = Lists.newArrayList();
        for (Class clazz : getClasses()) {
            if (isActionsClass(clazz)) {
                methods.addAll(getAnnotatedActionMethodsForClass(clazz));
            }
        }

        return methods;
    }

    private static List<Method> getAnnotatedActionMethodsForClass(Class clazz) {
        List<Method> annotatedMethods = Lists.newArrayList();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(ActionDefinition.class) != null) {
                annotatedMethods.add(method);
                LOGGER.debug("Found annotated method {} for package {}", method, method.getDeclaringClass().getPackage());
            }
        }

        return annotatedMethods;
    }

    private static List<Class> getClasses() throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = ActionsRegistrator.DEFAULT_ACTIONS_PACKAGE_NAME.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = Lists.newArrayList();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File file = new File(resource.getFile());
            LOGGER.debug("Found source directory for actions registration: {}", file.getAbsolutePath());
            dirs.add(file);
        }
        List<Class> classes = Lists.newArrayList();
        for (File directory : dirs) {
            System.out.println(directory.getAbsolutePath());
            if (directory.exists()) {
                classes.addAll(findClassesInDirectory(directory, ActionsRegistrator.DEFAULT_ACTIONS_PACKAGE_NAME));
            } else {
                //TODO: reduce the level of sacredness
                int begin = directory.getAbsolutePath().indexOf("file:\\") + 6;
                int end = directory.getAbsolutePath().indexOf("!");
                String jarName = directory.getAbsolutePath().substring(begin, end);
                classes.addAll(findClassesInJar(jarName, ActionsRegistrator.DEFAULT_ACTIONS_PACKAGE_NAME));
            }
        }

        return classes;
    }

    private static boolean isActionsClass(Class clazz) {
        if (clazz.getAnnotation(ActionClass.class) != null) {
            return true;
        }

        //class is handled also if it has no ActionClass annotation, but extends annotated superclass
        while (clazz != null) {
            if (clazz.getAnnotation(ActionClass.class) != null) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }

        return false;
    }

}
