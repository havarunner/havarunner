package com.github.havarunner

import org.junit._
import com.github.havarunner.exception.{NonStaticInnerClassMayNotHaveSuperclasses, ClassHasMultipleScenarioAnnotations, SuiteMemberDoesNotBelongToSuitePackage, UnsupportedAnnotationException}
import com.github.havarunner.Reflections._
import com.github.havarunner.annotation.{Scenarios, RunSequentially, PartOf}
import java.lang.annotation.Annotation

private[havarunner] object Validations {

  def reportInvalidations(implicit testAndParameters: TestAndParameters): Seq[_<:Exception] =
    suiteConfigError ++
    multipleScenariosError ++
    nonStaticInnerClassHasSuperclass ++
    unsupportedMethodAnnotations ++
    unsupportedClassAnnotations

  implicit def optionSeq2seq[T](optionSeq: Seq[Option[T]]): Seq[T] = optionSeq.flatMap(identity(_))
  implicit def optionException2Seq(optionException: Option[Exception]): Seq[Option[Exception]] = optionException :: Nil

  def multipleScenariosError(implicit testAndParameters: TestAndParameters): Option[ClassHasMultipleScenarioAnnotations] =
    if (findMethods(testAndParameters.testClass, classOf[Scenarios]).length > 1)
      Some(new ClassHasMultipleScenarioAnnotations(testAndParameters.testClass))
    else
      None

  def nonStaticInnerClassHasSuperclass(implicit testAndParameters: TestAndParameters): Option[NonStaticInnerClassMayNotHaveSuperclasses] =
    testAndParameters.encloser.flatMap(_ =>
      if (testAndParameters.testClass.getSuperclass == classOf[java.lang.Object])
        None
      else
        Some(new NonStaticInnerClassMayNotHaveSuperclasses(testAndParameters))
    )

  def suiteConfigError(implicit testAndParameters: TestAndParameters): Option[SuiteMemberDoesNotBelongToSuitePackage] =
    findAnnotationRecursively(testAndParameters.testClass, classOf[PartOf]) flatMap { (partOfAnnotation: Annotation) =>
      val suiteClass: Class[_ <: HavaRunnerSuite[_]] = partOfAnnotation.asInstanceOf[PartOf].value()
      if (!testAndParameters.testClass.getPackage.getName.startsWith(suiteClass.getPackage.getName))
        Some(new SuiteMemberDoesNotBelongToSuitePackage(testAndParameters.testClass, suiteClass))
      else
        None
    }

  def unsupportedMethodAnnotations(implicit testAndParameters: TestAndParameters): Seq[Option[UnsupportedAnnotationException]] =
    unsupportedJUnitAnnotations.map(annotationAndReason => {
      if (hasMethodAnnotatedWith(testAndParameters.testClass, annotationAndReason.annotationClass))
        Some(new UnsupportedAnnotationException(annotationAndReason.annotationClass, testAndParameters.testClass, annotationAndReason.customReason))
      else
        None
    })

  def unsupportedClassAnnotations(implicit testAndParameters: TestAndParameters): Seq[Option[UnsupportedAnnotationException]] =
    unsupportedJUnitAnnotations.flatMap(annotationAndReason => {
      testAndParameters.testClass.getAnnotations.filter(_.getClass == annotationAndReason.annotationClass).map(usedAnnotation =>
        Some(new UnsupportedAnnotationException(usedAnnotation.getClass, testAndParameters.testClass, annotationAndReason.customReason))
      )
    })

  def unsupportedJUnitAnnotations(implicit maybeSequential: MaybeSequential) =
    maybeSequential.runSequentially map { _ =>
      unsupportedAnnotationsWhenSequential
    } getOrElse
      allUnsupportedAnnotations

  val allUnsupportedAnnotations =
    AnnotationAndReason(classOf[After], Some(s"Only tests that are @${classOf[RunSequentially].getSimpleName} may use @${classOf[After].getSimpleName}")) ::
    AnnotationAndReason(classOf[Before], Some(s"Only tests that are @${classOf[RunSequentially].getSimpleName} may use @${classOf[Before].getSimpleName}")) ::
    AnnotationAndReason(classOf[AfterClass]) ::
    AnnotationAndReason(classOf[BeforeClass]) ::
    AnnotationAndReason(classOf[ClassRule]) :: Nil

  val unsupportedAnnotationsWhenSequential =
    allUnsupportedAnnotations
      .filterNot(_.annotationClass == classOf[After])
      .filterNot(_.annotationClass == classOf[Before])

  case class AnnotationAndReason(annotationClass: Class[_ <: Annotation], customReason: Option[String] = None)
}
