package com.github.havarunner.exception

class ClassHasMultipleScenarioAnnotations(testClass: Class[_]) extends RuntimeException(
  String.format(
    "Test %s has more than one @Scenario methods. Remove all but one of them.",
    testClass.getName
  )
)
