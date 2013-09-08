package havarunner;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

class ScenarioInterceptor implements MethodInterceptor {
    final Object scenario;

    ScenarioInterceptor(TestAndParameters testAndParameters) {
        this.scenario = testAndParameters.scenario;
    }

    @Override
    public Object intercept(Object proxiedObject, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return methodProxy.invokeSuper(proxiedObject, args);
    }
}
