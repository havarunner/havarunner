package com.github.havarunner

import org.junit._
import java.lang.reflect.{Method, Modifier, Member}
import scala.Some
import com.github.havarunner.exception.{UnsupportedAnnotationException, MemberIsNotPackagePrivateException, CamelCasedException}
import com.github.havarunner.Reflections._

private[havarunner] object CodingConventionsAndValidations {

  def reportInvalidations(testAndParameters: TestAndParameters): Option[Exception] =
    try {
      ensuringSnakeCased(testAndParameters.testMethod)
      ensuringPackagePrivate(testAndParameters.testMethod)
      ensuringValidTestClass(testAndParameters.testClass)
      None
    } catch {
      case e: Exception => Some(e)
    }

  private def ensuringSnakeCased(method: Method) {
    if (hasInvalidMethodName(method)) {
      throw new CamelCasedException(String.format(
        "Example %s is camed-cased. Please use_snake_cased_example_names.",
        method.getName
      ))
    }
  }

  private def ensuringPackagePrivate(method: Method) {
    if (isNotPackagePrivate(method)) {
      throw new MemberIsNotPackagePrivateException(method)
    }
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

  private def hasInvalidMethodName(method: Method) = {
    val methodName = method.getName
    methodName.matches(".*[a-z][A-Z].*") && !methodName.contains("_")
  }

  private def isNotPackagePrivate(member: Member) = {
    Modifier.isPrivate(member.getModifiers) ||
      Modifier.isPublic(member.getModifiers) ||
      Modifier.isProtected(member.getModifiers)
  }
}
