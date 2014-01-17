package com.github.havarunner.exception

import com.github.havarunner.TestAndParameters

class NonStaticInnerClassMayNotHaveSuperclasses(testAndParameters: TestAndParameters) extends RuntimeException(
  s"The non-static inner class ${testAndParameters.testClass.getName} should not have a superclass. (HavaRunner considers deep class hiearchies as conceptual burden that hinder understandability.)"
)
