package com.github.havarunner

import org.junit._
import scala.Some
import com.github.havarunner.exception.{SuiteMemberDoesNotBelongToSuitePackage, UnsupportedAnnotationException}
import com.github.havarunner.Reflections._
import com.github.havarunner.annotation.PartOf
import java.lang.annotation.Annotation

private[havarunner] object Validations {

  def reportInvalidations(implicit testAndParameters: TestAndParameters): Option[Exception] =
    suiteConfigError orElse
      reportUnsupportedClassAnnotations.find(_.isDefined).flatMap(identity) orElse
      reportUnsupportedMethodAnnotations.find(_.isDefined).flatMap(identity)

  private def suiteConfigError(implicit testAndParameters: TestAndParameters): Option[SuiteMemberDoesNotBelongToSuitePackage] =
    findAnnotationRecursively(testAndParameters.testClass, classOf[PartOf]) flatMap { (partOfAnnotation: Annotation) =>
      val suiteClass: Class[_ <: HavaRunnerSuite[_]] = partOfAnnotation.asInstanceOf[PartOf].value()
      if (!testAndParameters.testClass.getPackage.getName.startsWith(suiteClass.getPackage.getName))
        Some(new SuiteMemberDoesNotBelongToSuitePackage(testAndParameters.testClass, suiteClass))
      else
        None
    }

  private def reportUnsupportedMethodAnnotations(implicit testAndParameters: TestAndParameters): Seq[Option[UnsupportedAnnotationException]] =
    unsupportedJUnitAnnotations.map(unsupportedAnnotation => {
      if (hasMethodAnnotatedWith(testAndParameters.testClass, unsupportedAnnotation))
        Some(new UnsupportedAnnotationException(unsupportedAnnotation, testAndParameters.testClass))
      else
        None
    })

  private def reportUnsupportedClassAnnotations(implicit testAndParameters: TestAndParameters): Seq[Option[UnsupportedAnnotationException]] =
    unsupportedJUnitAnnotations.flatMap(unsupportedAnnotation => {
      testAndParameters.testClass.getAnnotations.filter(_.getClass == unsupportedAnnotation).map(usedAnnotation =>
        Some(new UnsupportedAnnotationException(usedAnnotation.getClass, testAndParameters.testClass))
      )
    })

  private val unsupportedJUnitAnnotations = Seq(
    classOf[After],
    classOf[Before],
    classOf[AfterClass],
    classOf[BeforeClass],
    classOf[Rule],
    classOf[ClassRule]
  )
}
