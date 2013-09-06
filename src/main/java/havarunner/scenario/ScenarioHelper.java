package havarunner.scenario;

import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ScenarioHelper {

    public static void addScenarioInterceptorAndRunTest(FrameworkMethodAndScenario frameworkMethodAndScenario, Object testClassInstance) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ScenarioInterceptor interceptor = new ScenarioInterceptor(frameworkMethodAndScenario);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(testClassInstance.getClass());
        enhancer.setCallback(interceptor);
        Object intercepted = enhancer.create();
        Method testMethod = intercepted.getClass().getDeclaredMethod(frameworkMethodAndScenario.getFrameworkMethod().getName());
        testMethod.setAccessible(true);
        testMethod.invoke(intercepted);
    }

    public static final Object defaultScenario = new Object();
}
