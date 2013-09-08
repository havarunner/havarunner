package havarunner;

import havarunner.scenario.TestParameters;
import havarunner.scenario.TestWithMultipleScenarios;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import static havarunner.scenario.ScenarioHelper.*;

class Helper {

    static List<TestParameters> toTestParameters(Collection<Class> classesToTest) {
        List<TestParameters> frameworkMethods = new ArrayList<>();
        for (Class aClass : classesToTest) {
            TestClass testClass = new TestClass(aClass);
            for (MethodAndScenario methodAndScenario : findTestMethods(testClass)) {
                frameworkMethods.add(
                    new TestParameters(
                        new FrameworkMethod(methodAndScenario.method),
                        testClass,
                        methodAndScenario.scenario,
                        findBefores(testClass)
                    )
                );
            }
        }
        return frameworkMethods;
    }

    private static Collection<Method> findBefores(TestClass testClass) {
        Collection<Method> befores = new ArrayList<>();
        for (Method method : testClass.getJavaClass().getDeclaredMethods()) {
            if (method.getAnnotation(Before.class) != null) {
                befores.add(method);
            }
        }
        return befores;
    }

    static Object newTestClassInstance(TestClass testClass) {
        try {
            return findOnlyConstructor(testClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Constructor findOnlyConstructor(TestClass testClass) {
        Constructor<?>[] declaredConstructors = testClass.getJavaClass().getDeclaredConstructors();
        Assert.assertEquals(
            String.format("The class %s should have exactly one no-arg constructor", testClass.getJavaClass().getName()),
            1,
            declaredConstructors.length
        );
        Constructor<?> declaredConstructor = declaredConstructors[0];
        declaredConstructor.setAccessible(true);
        return declaredConstructor;
    }

    private static List<MethodAndScenario> findTestMethods(TestClass testClass) {
        List<MethodAndScenario> testMethods = new ArrayList<>();
        for (Object scenario : scenarios(testClass)) {
            for (Method method : testClass.getJavaClass().getDeclaredMethods()) {
                if (method.getAnnotation(Test.class) != null) {
                    method.setAccessible(true);
                    testMethods.add(new MethodAndScenario(scenario, method));
                }
            }
        }

        return testMethods;
    }

    private static Set scenarios(TestClass testClass) {
        if (isScenarioClass(testClass.getJavaClass())) {
            return ((TestWithMultipleScenarios) newTestClassInstance(testClass)).scenarios();
        } else {
            return Collections.singleton(defaultScenario);
        }
    }

    static boolean isScenarioClass(Class clazz) {
        return TestWithMultipleScenarios.class.isAssignableFrom(clazz);
    }

    private static class MethodAndScenario {
        private final Object scenario;
        private final Method method;

        public MethodAndScenario(Object scenario, Method method) {
            this.scenario = scenario;
            this.method = method;
        }
    }
}
