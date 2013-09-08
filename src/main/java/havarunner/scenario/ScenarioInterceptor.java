package havarunner.scenario;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.Test;

import java.lang.reflect.Method;

class ScenarioInterceptor implements MethodInterceptor {
    final Object scenario;

    ScenarioInterceptor(TestParameters testParameters) {
        this.scenario = testParameters.getScenario();
    }

    @Override
    public Object intercept(Object proxiedObject, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return methodProxy.invokeSuper(proxiedObject, args);
    }
}
