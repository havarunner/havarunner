package com.github.havarunner

import org.junit._
import java.lang.reflect.{Method, Modifier, Member}
import scala.Some
import com.github.havarunner.exception.{UnsupportedAnnotationException, MemberIsNotPackagePrivateException, CamelCasedException}
import com.github.havarunner.Reflections._

private[havarunner] object Validations {

  def reportInvalidations(implicit testAndParameters: TestAndParameters): Option[Exception] =
    try {
      ensuringValidTestClass(testAndParameters.testClass)
      None
    } catch {
      case e: Exception => Some(e)
    }

  private def ensuringValidTestClass(testClass: Class[_]) {
    ensureDoesNotHaveUnsupportedJUnitAnnotations(testClass)
  }

  private def ensureDoesNotHaveUnsupportedJUnitAnnotations(testClass: Class[_])  {
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
