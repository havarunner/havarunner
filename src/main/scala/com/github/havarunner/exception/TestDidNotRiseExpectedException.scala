package com.github.havarunner.exception

import com.github.havarunner.TestAndParameters

class TestDidNotRiseExpectedException(expected: Class[_<:Throwable], testAndParameters: TestAndParameters) extends RuntimeException(
  s"Test ${testAndParameters.testClass.getJavaClass.getName}#${testAndParameters.frameworkMethod.getMethod.getName} did not throw the expected exception ${expected.getName}"
)