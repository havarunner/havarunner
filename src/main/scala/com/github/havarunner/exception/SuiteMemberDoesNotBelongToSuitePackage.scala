package com.github.havarunner.exception

import com.github.havarunner.HavaRunnerSuite

class SuiteMemberDoesNotBelongToSuitePackage(suiteMember: Class[_], suiteClass: Class[_<:HavaRunnerSuite[_]]) extends RuntimeException(
  String.format(
    "Suite member %s must be within the same package as the suite %s. Try moving %s under the package %s.",
    suiteMember.getName,
    suiteClass.getName,
    suiteMember.getSimpleName,
    suiteClass.getPackage.getName
  )
)
