package com.github.havarunner.exception

class ConstructorNotFound(clazz: Class[_], original: NoSuchMethodException) extends RuntimeException(
  String.format(
    "Class %s is missing the required constructor. Try adding the following constructor: %s",
    clazz.getSimpleName,
    original.getMessage
  )
)
