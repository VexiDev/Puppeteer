package gg.vexi.Puppeteer.annotations.Scanner;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class AnnotationScanner {

    public static List<Class<?>> findAnnotatedClasses(String packageName, Class<? extends Annotation> annotation) throws IOException, ClassNotFoundException {
        List<Class<?>> allClasses = ClasspathScanner.getClasses(packageName);
        List<Class<?>> annotatedClasses = new ArrayList<>();
        
        for (Class<?> singleClass : allClasses) {
            // System.out.println("Scanning class "+singleClass.getSimpleName());
            if (singleClass.isAnnotationPresent(annotation)) {
                // System.out.println("Found annotation in class "+singleClass.getSimpleName());
                annotatedClasses.add(singleClass);
            }
        }
        
        return annotatedClasses;
    }
}
