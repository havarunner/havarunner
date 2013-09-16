package com.github.havarunner

import org.junit._
import scala.Some
import com.github.havarunner.exception.{SuiteMemberDoesNotBelongToSuitePackage, UnsupportedAnnotationException}
import com.github.havarunner.Reflections._
import com.github.havarunner.annotation.PartOf

private[havarunner] object Validations {

  def reportInvalidations(implicit testAndParameters: TestAndParameters): Option[Exception] =
    try {
      ensureDoesNotHaveUnsupportedJUnitAnnotations(testAndParameters.testClass)
      ensureHasValidSuiteConfig(testAndParameters.testClass)
      None
    } catch {
      case e: Exception => Some(e)
    }

  private def ensureHasValidSuiteConfig(testClass: Class[_]) {
    if (isAnnotatedWith(testClass, classOf[PartOf])) {
      val suiteClass: Class[_ <: HavaRunnerSuite[_]] = testClass.getAnnotation(classOf[PartOf]).value()
      if (!testClass.getPackage.getName.startsWith(suiteClass.getPackage.getName)) {
        throw new SuiteMemberDoesNotBelongToSuitePackage(testClass, suiteClass)
      }
    }
  }

  private def ensureDoesNotHaveUnsupportedJUnitAnnotations(testClass: Class[_]) {
    val unsupportedJUnitAnnotations = Seq(
      classOf[After],
      classOf[Before],
      classOf[AfterClass],
      classOf[BeforeClass],
      classOf[Rule],
      classOf[ClassRule]
    )

    unsupportedJUnitAnnotations.foreach(unsupportedJUnitAnnotation => {
      testClass.getAnnotations.foreach(classAnnotation =>
        if (unsupportedJUnitAnnotation == classAnnotation.getClass) {
          throw new UnsupportedAnnotationException(classAnnotation.getClass, testClass)
        }
      )
      if (hasMethodAnnotatedWith(testClass, unsupportedJUnitAnnotation)) {
        throw new UnsupportedAnnotationException(unsupportedJUnitAnnotation, testClass)
      }
    })
  }
}
