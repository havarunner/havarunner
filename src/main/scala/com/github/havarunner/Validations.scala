package com.github.havarunner

import org.junit._
import scala.Some
import com.github.havarunner.exception.{SuiteMemberDoesNotBelongToSuitePackage, UnsupportedAnnotationException}
import com.github.havarunner.Reflections._
import com.github.havarunner.annotation.{RunSequentially, PartOf}
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
    unsupportedJUnitAnnotations.map(annotationAndReason => {
      if (hasMethodAnnotatedWith(testAndParameters.testClass, annotationAndReason.annotationClass))
        Some(new UnsupportedAnnotationException(annotationAndReason.annotationClass, testAndParameters.testClass, annotationAndReason.customReason))
      else
        None
    })

  private def reportUnsupportedClassAnnotations(implicit testAndParameters: TestAndParameters): Seq[Option[UnsupportedAnnotationException]] =
    unsupportedJUnitAnnotations.flatMap(annotationAndReason => {
      testAndParameters.testClass.getAnnotations.filter(_.getClass == annotationAndReason.annotationClass).map(usedAnnotation =>
        Some(new UnsupportedAnnotationException(usedAnnotation.getClass, testAndParameters.testClass, annotationAndReason.customReason))
      )
    })

  private def unsupportedJUnitAnnotations(implicit maybeSequential: MaybeSequential) =
    if (maybeSequential.runSequentially)
      unsupportedAnnotationsWhenSequential
    else
      allUnsupportedAnnotations

  private val allUnsupportedAnnotations =
    AnnotationAndReason(classOf[After], Some(s"Only tests that are @${classOf[RunSequentially].getSimpleName} may use @${classOf[After].getSimpleName}")) ::
    AnnotationAndReason(classOf[Before], Some(s"Only tests that are @${classOf[RunSequentially].getSimpleName} may use @${classOf[Before].getSimpleName}")) ::
    AnnotationAndReason(classOf[AfterClass]) ::
    AnnotationAndReason(classOf[BeforeClass]) ::
    AnnotationAndReason(classOf[ClassRule]) :: Nil

  /**
   * Sequential tests may use the @After and @Before methods.
   */
  private val unsupportedAnnotationsWhenSequential =
    allUnsupportedAnnotations
      .filterNot(_.annotationClass == classOf[After])
      .filterNot(_.annotationClass == classOf[Before])

  private case class AnnotationAndReason(annotationClass: Class[_ <: Annotation], customReason: Option[String] = None)
}
