package havarunner.scenario;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

class ScenarioInterceptor implements MethodInterceptor {
    final Object scenario;

    ScenarioInterceptor(FrameworkMethodAndScenario frameworkMethodAndScenario) {
        this.scenario = frameworkMethodAndScenario.getScenario();
    }

    @Override
    public Object intercept(Object proxiedObject, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (method.getName().equals("currentScenario") && args == null) {
            return scenario;
        } else {
            return methodProxy.invokeSuper(proxiedObject, args);
        }
    }
}
