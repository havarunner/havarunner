package havarunner.scenario;

import net.sf.cglib.proxy.Enhancer;
import org.junit.runners.model.Statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ScenarioHelper {

    public static Statement addScenarioInterceptor(FrameworkMethodAndScenario frameworkMethodAndScenario, Object testClassInstance) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ScenarioInterceptor interceptor = new ScenarioInterceptor(frameworkMethodAndScenario);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(testClassInstance.getClass());
        enhancer.setCallback(interceptor);
        final Object intercepted = enhancer.create();
        final Method testMethod = intercepted.getClass().getDeclaredMethod(frameworkMethodAndScenario.getFrameworkMethod().getName());
        testMethod.setAccessible(true);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                testMethod.invoke(intercepted);
            }
        };
    }

    public static final Object defaultScenario = new Object();
}
