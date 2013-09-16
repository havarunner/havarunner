package com.github.havarunner

/**
 * Implementations of this interface are tests that are relevant in multiple scenarios.
 *
 * @author Lauri Lehmijoki
 */
trait TestWithMultipleScenarios[T] {
  /**
   * @return all the scenarios against which the tests will be run
   */
  def scenarios[T]: java.util.Set[T]
}
