package havarunner

import org.junit.runners.model.{FrameworkMethod, TestClass}
import havarunner.exception._
import org.junit._
import java.lang.reflect.{Method, Modifier, Member}
import scala.Some

private[havarunner] object CodingConventionsAndValidations {

  def reportInvalidations(testAndParameters: TestAndParameters): Option[Exception] =
    try {
      ensuringSnakeCased(testAndParameters.frameworkMethod)
      ensuringPackagePrivate(testAndParameters.frameworkMethod)
      ensuringValidTestClass(testAndParameters.testClass)
      testAndParameters.beforeClasses.foreach(ensureMethodIsStatic(_, testAndParameters.testClass.getJavaClass))
      testAndParameters.afterClasses.foreach(ensureMethodIsStatic(_, testAndParameters.testClass.getJavaClass))
      None
    } catch {
      case e: Exception => Some(e)
    }

  def ensureMethodIsStatic(method: Method, clazz: Class[_ <: Any]) {
    if (!Modifier.isStatic(method.getModifiers)) {
      throw new MethodIsNotStatic(method, clazz)
    }
  }

  private def ensuringSnakeCased(frameworkMethod: FrameworkMethod) {
    if (hasInvalidMethodName(frameworkMethod)) {
      throw new CamelCasedException(String.format(
        "Example %s is camed-cased. Please use_snake_cased_example_names.",
        frameworkMethod.getName
      ))
    }
  }

  private def ensuringPackagePrivate(frameworkMethod: FrameworkMethod) {
    if (isNotPackagePrivate(frameworkMethod.getMethod)) {
      throw new MemberIsNotPackagePrivateException(frameworkMethod.getMethod)
    }
  }

  private def ensuringValidTestClass(testClass: TestClass) {
    ensureDoesNotHaveUnsupportedJUnitAnnotations(testClass)
  }

  private def ensureDoesNotHaveUnsupportedJUnitAnnotations(testClass: TestClass)  {
    val unsupportedJUnitAnnotations = Seq(
      classOf[After],
      classOf[Rule],
      classOf[ClassRule]
    )

    unsupportedJUnitAnnotations.foreach(unsupportedJUnitAnnotation => {
      testClass.getAnnotations.foreach(classAnnotation =>
        if (unsupportedJUnitAnnotation == classAnnotation.getClass) {
          throw new UnsupportedAnnotationException(classAnnotation.getClass, testClass.getJavaClass)
        }
      )
      if (!testClass.getAnnotatedMethods(unsupportedJUnitAnnotation).isEmpty) {
        throw new UnsupportedAnnotationException(unsupportedJUnitAnnotation, testClass.getJavaClass)
      }
    })
  }

  private def hasInvalidMethodName(frameworkMethod: FrameworkMethod) = {
    val methodName = frameworkMethod.getMethod.getName
    methodName.matches(".*[a-z][A-Z].*") && !methodName.contains("_")
  }

  private def isNotPackagePrivate(member: Member) = {
    Modifier.isPrivate(member.getModifiers) ||
      Modifier.isPublic(member.getModifiers) ||
      Modifier.isProtected(member.getModifiers)
  }
}
