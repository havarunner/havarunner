package com.github.havarunner

import org.junit._
import scala.Some
import com.github.havarunner.exception.{NonStaticInnerClassException, SuiteMemberDoesNotBelongToSuitePackage, UnsupportedAnnotationException}
import com.github.havarunner.Reflections._
import com.github.havarunner.annotation.{RunSequentially, PartOf}
import java.lang.annotation.Annotation
import java.lang.reflect.Modifier

private[havarunner] object Validations {

  def reportInvalidations(implicit testAndParameters: TestAndParameters): Seq[_<:Exception] =
    suiteConfigError ++
    nonStaticInnerClass ++
    unsupportedMethodAnnotations ++
    unsupportedClassAnnotations

  private implicit def optionSeq2seq[T](optionSeq: Seq[Option[T]]): Seq[T] = optionSeq.flatMap(identity(_))
  private implicit def optionException2Seq(optionException: Option[Exception]): Seq[Option[Exception]] = optionException :: Nil

  private def nonStaticInnerClass(implicit testAndParameters: TestAndParameters): Option[NonStaticInnerClassException] =
    if (testAndParameters.testClass.getDeclaringClass != null && !Modifier.isStatic(testAndParameters.testClass.getModifiers))
      Some(new NonStaticInnerClassException(testAndParameters.testClass))
    else
      None

  private def suiteConfigError(implicit testAndParameters: TestAndParameters): Option[SuiteMemberDoesNotBelongToSuitePackage] =
    findAnnotationRecursively(testAndParameters.testClass, classOf[PartOf]) flatMap { (partOfAnnotation: Annotation) =>
      val suiteClass: Class[_ <: HavaRunnerSuite[_]] = partOfAnnotation.asInstanceOf[PartOf].value()
      if (!testAndParameters.testClass.getPackage.getName.startsWith(suiteClass.getPackage.getName))
        Some(new SuiteMemberDoesNotBelongToSuitePackage(testAndParameters.testClass, suiteClass))
      else
        None
    }

  private def unsupportedMethodAnnotations(implicit testAndParameters: TestAndParameters): Seq[Option[UnsupportedAnnotationException]] =
    unsupportedJUnitAnnotations.map(annotationAndReason => {
      if (hasMethodAnnotatedWith(testAndParameters.testClass, annotationAndReason.annotationClass))
        Some(new UnsupportedAnnotationException(annotationAndReason.annotationClass, testAndParameters.testClass, annotationAndReason.customReason))
      else
        None
    })

  private def unsupportedClassAnnotations(implicit testAndParameters: TestAndParameters): Seq[Option[UnsupportedAnnotationException]] =
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

  private val unsupportedAnnotationsWhenSequential =
    allUnsupportedAnnotations
      .filterNot(_.annotationClass == classOf[After])
      .filterNot(_.annotationClass == classOf[Before])

  private case class AnnotationAndReason(annotationClass: Class[_ <: Annotation], customReason: Option[String] = None)
}
