package com.github.havarunner

private[havarunner] case class Operation[T](op: () => T) {
  def run: T = op()
}