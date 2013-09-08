package havarunner;

import havarunner.exception.ScenarioMethodNotFound;
import net.sf.cglib.proxy.Enhancer;
import org.junit.runners.model.Statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ScenarioHelper {

    static Statement addScenarioInterceptor(final TestAndParameters testAndParameters, Object testClassInstance) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ScenarioInterceptor interceptor = new ScenarioInterceptor(testAndParameters);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(testClassInstance.getClass());
        enhancer.setCallback(interceptor);
        final Object intercepted = enhancer.create();
        final Method testMethod = findScenarioTestMethod(testAndParameters, intercepted);
        return withBefores(
            testRunningStatement(testAndParameters, intercepted, testMethod),
            testAndParameters,
            intercepted
        );
    }

    private static Statement testRunningStatement(final TestAndParameters testAndParameters, final Object intercepted, final Method testMethod) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                testMethod.setAccessible(true);
                testMethod.invoke(intercepted, testAndParameters.scenario);
            }
        };
    }

    private static Statement withBefores(final Statement statement, final TestAndParameters testAndParameters, final Object intercepted) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                for (Method before : testAndParameters.befores) {
                    before.setAccessible(true);
                    before.invoke(intercepted);
                }
                statement.evaluate();
            }
        };
    }

    private static Method findScenarioTestMethod(TestAndParameters testAndParameters, Object intercepted) throws NoSuchMethodException {
        try {
            Method scenarioMethod = intercepted.
                getClass().
                getDeclaredMethod(
                    testAndParameters.frameworkMethod.getName(),
                    testAndParameters.scenario.getClass()
                );
            return scenarioMethod;
        } catch (NoSuchMethodException e) {
            String methodAndSignature = String.format(
                "%s(%s)",
                testAndParameters.frameworkMethod.getName(),
                testAndParameters.scenario.getClass().getName()
            );
            throw new ScenarioMethodNotFound(
                String.format(
                    "Could not find the scenario method %s#%s. Please add the method %s into class %s.",
                    testAndParameters.testClass.getJavaClass().getSimpleName(),
                    methodAndSignature,
                    methodAndSignature,
                    testAndParameters.testClass.getJavaClass().getName()
                )
            );
        }
    }

    static final Object defaultScenario = new Object();
}
