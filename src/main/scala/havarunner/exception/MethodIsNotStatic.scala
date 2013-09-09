package havarunner.exception

import java.lang.reflect.Method

class MethodIsNotStatic(method: Method, clazz: Class[_ <: Any]) extends RuntimeException(
  String.format(
    "Method %s#%s should be static",
    clazz.getSimpleName,
    method.getName
  )
)
