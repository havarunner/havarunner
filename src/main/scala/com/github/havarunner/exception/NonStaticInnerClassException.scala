package com.github.havarunner.exception

class NonStaticInnerClassException(clazz: Class[_]) extends RuntimeException(
  s"The class ${clazz.getName} must be static (HavaRunner does not support non-static inner classes)"
)
