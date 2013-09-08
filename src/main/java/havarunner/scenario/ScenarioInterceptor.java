package havarunner.scenario;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

class ScenarioInterceptor implements MethodInterceptor {
    final Object scenario;

    ScenarioInterceptor(TestParameters testParameters) {
        this.scenario = testParameters.getScenario();
    }

    @Override
    public Object intercept(Object proxiedObject, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (method.getName().equals("currentScenario") && args.length == 0) {
            return scenario;
        } else {
            return methodProxy.invokeSuper(proxiedObject, args);
        }
    }
}
