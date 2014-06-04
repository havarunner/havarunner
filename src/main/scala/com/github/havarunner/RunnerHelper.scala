package com.github.havarunner

import org.junit.runner.manipulation.Filter
import org.junit.runner.Description
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
 * Place here code that is indirectly related to running tests.
 */
private[havarunner] object RunnerHelper {

  def acceptTest(testParameters: TestAndParameters, filterOption: Option[Filter]): Boolean =
    filterOption.fold(true)(filter => {
      val FilterDescribePattern = "Method (.*)\\((.*)\\)".r
      filter.describe() match {
        case FilterDescribePattern(desiredMethodName, desiredClassName) =>
          val methodNameMatches = testParameters.testMethod.getName.equals(desiredMethodName)
          val classNameMatches: Boolean = testParameters.testClass.getName.equals(desiredClassName)
          classNameMatches && methodNameMatches
        case unexpected => throw new IllegalArgumentException(s"Filter#describe returned an unexpected string $unexpected")
      }
    })

  def describeTest(implicit testAndParameters: TestAndParameters) =
    Description createTestDescription(
      testAndParameters.testClass,
      testAndParameters.testMethod.getName + testAndParameters.scenarioToString
    )

  def reportIfSuite(tests: java.lang.Iterable[TestAndParameters]): java.lang.Iterable[String] =
    tests
      .flatMap(testAndParams =>
        testAndParams.partOf.map(suiteClass =>
          s"[HavaRunner] Running ${testAndParams.toStringWithoutSuite} as a part of ${suiteClass.getSimpleName}"
        )
      ).asJava // To ease testing on the Java side
}
