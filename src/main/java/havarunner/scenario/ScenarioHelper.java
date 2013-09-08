package havarunner.scenario;

import havarunner.exception.ScenarioMethodNotFound;
import net.sf.cglib.proxy.Enhancer;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ScenarioHelper {

    public static Statement addScenarioInterceptor(final TestParameters testParameters, Object testClassInstance) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ScenarioInterceptor interceptor = new ScenarioInterceptor(testParameters);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(testClassInstance.getClass());
        enhancer.setCallback(interceptor);
        final Object intercepted = enhancer.create();
        final Method testMethod = findScenarioTestMethod(testParameters, intercepted);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                testMethod.setAccessible(true);
                testMethod.invoke(intercepted, testParameters.getScenario());
            }
        };
    }

    private static Method findScenarioTestMethod(TestParameters testParameters, Object intercepted) throws NoSuchMethodException {
        try {
            Method scenarioMethod = intercepted.
                getClass().
                getDeclaredMethod(
                    testParameters.getFrameworkMethod().getName(),
                    testParameters.getScenario().getClass()
                );
            return scenarioMethod;
        } catch (NoSuchMethodException e) {
            String methodAndSignature = String.format(
                "%s(%s)",
                testParameters.getFrameworkMethod().getName(),
                testParameters.getScenario().getClass().getName()
            );
            throw new ScenarioMethodNotFound(
                String.format(
                    "Could not find the scenario method %s#%s. Please add the method %s into class %s.",
                    testParameters.getTestClass().getJavaClass().getSimpleName(),
                    methodAndSignature,
                    methodAndSignature,
                    testParameters.getTestClass().getJavaClass().getName()
                )
            );
        }
    }

    public static final Object defaultScenario = new Object();
}
