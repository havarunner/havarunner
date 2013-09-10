package havarunner

trait TestWithMultipleScenarios[T] { // TODO remove
  /**
   * @return all the scenarios in which the tests will be run
   */
  def scenarios[T]: java.util.Set[T]
}
