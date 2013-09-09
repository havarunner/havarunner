package havarunner

import net.sf.cglib.proxy.{MethodProxy, MethodInterceptor, Enhancer}
import java.lang.reflect.Method

private[havarunner] object CodeGeneratorHelper {
  def newEnhancedInstance(clazz: Class[_ <: Any]): AnyRef = {
    val interceptor = new Interceptor()
    val enhancer = new Enhancer()
    enhancer.setSuperclass(clazz)
    enhancer.setCallback(interceptor)
    enhancer.create()
  }

  class Interceptor extends MethodInterceptor {
    def intercept(proxiedObject: Object, method: Method, args: Array[AnyRef], methodProxy: MethodProxy) =
      methodProxy.invokeSuper(proxiedObject, args)
  }
}
