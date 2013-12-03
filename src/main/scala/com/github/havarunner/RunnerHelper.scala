package com.github.havarunner

import org.junit.runner.manipulation.Filter
import org.junit.runner.Description

private[havarunner] object RunnerHelper {

  def acceptTest(testParameters: TestAndParameters, filterOption: Option[Filter]): Boolean =
    filterOption.map(filter => {
      val FilterDescribePattern = "Method (.*)\\((.*)\\)".r
      filter.describe() match {
        case FilterDescribePattern(desiredMethodName, desiredClassName) =>
          val methodNameMatches = testParameters.testMethod.getName.equals(desiredMethodName)
          val classNameMatches: Boolean = testParameters.testClass.getName.equals(desiredClassName)
          classNameMatches && methodNameMatches
        case unexpected => throw new IllegalArgumentException(s"Filter#describe returned an unexpected string $unexpected")
      }
    }).getOrElse(true)

  def describeTest(implicit testAndParameters: TestAndParameters) =
    Description createTestDescription(
      testAndParameters.testClass,
      testAndParameters.testMethod.getName + testAndParameters.scenarioToString
    )

  def reportIfSuite(tests: Iterable[TestAndParameters]) =
    tests
      .flatMap(testAndParams =>
        testAndParams.partOf.map(suiteClass =>
          s"[HavaRunner] Running ${testAndParams.toStringWithoutSuite} as a part of ${suiteClass.getSimpleName}"
        )
      )
}
