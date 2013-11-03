package com.github.havarunner.exception

class MultipleInvalidations(val exceptions: Seq[Exception]) extends RuntimeException(
  exceptions.mkString("\n")
)
