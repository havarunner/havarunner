package com.github.havarunner

import java.lang.annotation.Annotation
import java.lang.reflect.{Modifier, AccessibleObject, Field, Method}
import com.github.havarunner.exception.ConstructorNotFound
import com.github.havarunner.TestInstanceCache._
import com.github.havarunner.Parser.ParseResult

/**
 * Place here code that is related to Java reflections but not to parsing tests.
 */
private[havarunner] object Reflections {
  def findAnnotationRecursively(clazz: Class[_], annotationClass: Class[_ <: Annotation]): Option[Annotation] =
    Option(clazz.getAnnotation(annotationClass)) orElse {
      Option(clazz.getSuperclass) flatMap {
        superclass => findAnnotationRecursively(superclass, annotationClass)
      }
    }

  def instantiate(implicit suiteInstanceOption: Option[HavaRunnerSuite[_]], testAndParameters: TestAndParameters, parseResult: ParseResult): Any = {
    val (constructor, argsOption) = resolveConstructorAndArgs
    val accessibleConstructor = ensureAccessible(constructor)
    argsOption match {
      case Nil  => accessibleConstructor.newInstance()
      case args => accessibleConstructor.newInstance(args.asInstanceOf[Seq[AnyRef]]:_*)
    }
  }

  def findDeclaredClasses(clazz: Class[_], accumulator: Seq[Class[_]] = Seq()): Seq[Class[_]] =
    if (clazz.getDeclaredClasses.isEmpty) {
      clazz +: accumulator
    } else {
      clazz +: clazz.getDeclaredClasses.flatMap(findDeclaredClasses(_, accumulator))
    }

  def resolveConstructorAndArgs(implicit testAndParameters: TestAndParameters, suiteInstanceOption: Option[HavaRunnerSuite[_]], parseResult: ParseResult) =
    withHelpfulConstructorMissingReport {
      val enclosingInstance: Option[_] = testAndParameters
        .encloser map { enclosingClass =>
          parseResult
            .find(candidate => {
              val isEnclosingAndHasSameScenario = candidate.testClass == enclosingClass && candidate.scenario.equals(testAndParameters.scenario)
              isEnclosingAndHasSameScenario
            })
            .map(instantiateTestClass(_, parseResult).instance)
            .getOrElse {
              enclosingClass.newInstance()
            }
        }
      def resolveConstructorArgs: Seq[_] = {
        val suiteAndScenarioArgs: Seq[_] = (suiteInstanceOption, testAndParameters.scenario) match {
          case (Some(suite), Some(scenario)) =>
            suite.suiteObject :: scenario :: Nil
          case (Some(suite), None) =>
            suite.suiteObject :: Nil
          case (None, Some(scenario)) =>
            scenario :: Nil
          case (None, None) =>
            Nil
        }
        val constructorArgs = enclosingInstance.map(_ +: suiteAndScenarioArgs).getOrElse(suiteAndScenarioArgs)
        constructorArgs
      }
      val constructorArgs: Seq[_] = testAndParameters
        .encloser
        .map(_ => enclosingInstance.get :: Nil) // Pass only the outer instance to non-static inner classes
        .getOrElse(resolveConstructorArgs)
      Pair(
        testAndParameters.testClass.getDeclaredConstructor(constructorArgs.map(_.getClass):_*),
        constructorArgs
      )
    }

  def withHelpfulConstructorMissingReport[T](op: => T)(implicit testAndParameters: TestAndParameters) =
    try {
      op
    } catch {
      case e: NoSuchMethodException =>
        throw new ConstructorNotFound(testAndParameters.testClass, e)
    }

  def enclosingClassForNonStaticTestClass(testAndParameters: TestAndParameters): Option[Class[_]] =
    if (!Modifier.isStatic(testAndParameters.testClass.getModifiers))
      Option(testAndParameters.testClass.getEnclosingClass)
    else
      None

  def findMethods(clazz: Class[_], annotation: Class[_ <: Annotation]): Seq[Method] =
    classWithSuperclasses(clazz).flatMap(clazz =>
      clazz.getDeclaredMethods.filter(_.getAnnotation(annotation) != null)
    )

  def findFields(clazz: Class[_], annotation: Class[_ <: Annotation]): Seq[Field] =
    classWithSuperclasses(clazz).flatMap(clazz =>
      clazz.getDeclaredFields.filter(_.getAnnotation(annotation) != null)
    )

  def invoke(method: Method)(implicit testInstance: TestInstance) =
    ensureAccessible(method).invoke(testInstance.instance)

  def classWithSuperclasses(clazz: Class[_], superclasses: Seq[Class[_]] = Nil): Seq[Class[_]] =
    if (clazz.getSuperclass != null)
      classWithSuperclasses(clazz.getSuperclass, clazz +: superclasses)
    else
      superclasses

  def hasMethodAnnotatedWith(clazz: Class[_], annotationClass: Class[_ <: Annotation]) =
    classWithSuperclasses(clazz).
      flatMap(_.getDeclaredMethods).
      exists(_.getAnnotation(annotationClass) != null)

  def ensureAccessible[T <: AccessibleObject](accessibleObject: T) = {
    accessibleObject.setAccessible(true)
    accessibleObject
  }
}
