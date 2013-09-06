package havarunner;

import havarunner.exceptions.CamelCasedException;
import havarunner.exceptions.MethodIsNotPackagePrivateException;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

class Helpers {

    static FrameworkMethod ensuringSnakeCased(FrameworkMethod frameworkMethod) {
        if (hasInvalidMethodName(frameworkMethod)) {
            throw new CamelCasedException(String.format(
                "Example %s is camed-cased. Please use_snake_cased_example_names.",
                frameworkMethod.getName()
            ));
        }
        return frameworkMethod;
    }

    static FrameworkMethod ensuringPackagePrivate(FrameworkMethod frameworkMethod) {
        if (
            Modifier.isPrivate(frameworkMethod.getMethod().getModifiers()) ||
            Modifier.isPublic(frameworkMethod.getMethod().getModifiers()) ||
            Modifier.isProtected(frameworkMethod.getMethod().getModifiers())
            ) {
            String accessModifier =
                Modifier.isPrivate(frameworkMethod.getMethod().getModifiers()) ? "private" :
                (Modifier.isPublic(frameworkMethod.getMethod().getModifiers()) ? "public" : "protected");
            throw new MethodIsNotPackagePrivateException(String.format(
                "Example %s is %s. Please make it package private.",
                frameworkMethod.getName(),
                accessModifier
            ));
        }
        return frameworkMethod;
    }

    private static boolean hasInvalidMethodName(FrameworkMethod frameworkMethod) {
        String methodName = frameworkMethod.getMethod().getName();
        return methodName.matches(".*[a-z][A-Z].*") && !methodName.contains("_");
    }

    static List<FrameworkMethod> toFrameworkMethods(TestClass testClass) {
        List<FrameworkMethod> frameworkMethods = new ArrayList<>();
        for (Method method : findTestMethods(testClass)) {
            frameworkMethods.add(
                ensuringSnakeCased(
                    ensuringPackagePrivate(
                        new FrameworkMethod(method)
                    )
                )
            );
        }
        return frameworkMethods;
    }

    static Object newTestClassInstance(TestClass testClass) {
        try {
            return testClass.getOnlyConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Method> findTestMethods(TestClass testClass) {
        List<Method> testMethods = new ArrayList<>();
        for (Method method : testClass.getJavaClass().getDeclaredMethods()) {
            if (method.getAnnotation(Test.class) != null) {
                method.setAccessible(true);
                testMethods.add(method);
            }
        }
        return testMethods;
    }
}
