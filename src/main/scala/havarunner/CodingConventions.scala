package havarunner

import org.junit.runners.model.{FrameworkMethod, TestClass}
import com.google.common.base.Optional
import havarunner.exception.{UnsupportedAnnotationException, MemberIsNotPackagePrivateException, CamelCasedException, CodingConventionException}
import org.junit._
import java.lang.reflect.{Modifier, Member}

private[havarunner] object CodingConventions {

  def violatesCodingConventions(testAndParameters: TestAndParameters,
                                testClass: TestClass) =
    try {
      ensuringSnakeCased(testAndParameters.frameworkMethod)
      ensuringPackagePrivate(testAndParameters.frameworkMethod)
      ensuringValidTestClass(testClass)
      Optional absent()
    } catch {
      case e: CodingConventionException => Optional of e
    }

  private def ensuringSnakeCased(frameworkMethod: FrameworkMethod) =
    if (hasInvalidMethodName(frameworkMethod)) {
      throw new CamelCasedException(String.format(
        "Example %s is camed-cased. Please use_snake_cased_example_names.",
        frameworkMethod.getName
      ))
    } else {
      frameworkMethod
    }

  private def ensuringPackagePrivate(frameworkMethod: FrameworkMethod) =
    if (isNotPackagePrivate(frameworkMethod.getMethod)) {
      throw new MemberIsNotPackagePrivateException(frameworkMethod.getMethod)
    } else {
      frameworkMethod
    }

  private def ensuringValidTestClass(testClass: TestClass) =
    ensureDoesNotHaveUnsupportedJUnitAnnotations(testClass)

  private def ensureDoesNotHaveUnsupportedJUnitAnnotations(testClass: TestClass) = {
    val unsupportedJUnitAnnotations = Seq(
      classOf[BeforeClass],
      classOf[After],
      classOf[AfterClass],
      classOf[Rule],
      classOf[ClassRule],
      classOf[FixMethodOrder]
    )

    unsupportedJUnitAnnotations.foreach(unsupportedJUnitAnnotation => {
      testClass.getAnnotations.foreach(classAnnotation =>
        if (unsupportedJUnitAnnotation == classAnnotation.getClass) {
          throw new UnsupportedAnnotationException(classAnnotation.getClass, testClass)
        }
      )
      if (!testClass.getAnnotatedMethods(unsupportedJUnitAnnotation).isEmpty) {
        throw new UnsupportedAnnotationException(unsupportedJUnitAnnotation, testClass)
      }
    })

    testClass
  }

  private def hasInvalidMethodName(frameworkMethod: FrameworkMethod) = {
    val methodName = frameworkMethod.getMethod().getName
    methodName.matches(".*[a-z][A-Z].*") && !methodName.contains("_")
  }

  private def isNotPackagePrivate(member: Member) = {
    Modifier.isPrivate(member.getModifiers()) ||
      Modifier.isPublic(member.getModifiers) ||
      Modifier.isProtected(member.getModifiers)
  }
}
