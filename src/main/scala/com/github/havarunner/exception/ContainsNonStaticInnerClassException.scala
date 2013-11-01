package com.github.havarunner.exception

class ContainsNonStaticInnerClassException(clazz: Class[_]) extends RuntimeException(
  s"The class ${clazz.getName} must be static (HavaRunner does not support non-static inner classes)"
)
