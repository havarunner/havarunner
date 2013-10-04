package com.github.havarunner

import java.lang.annotation.Annotation
import java.lang.reflect.{InvocationTargetException, Method}
import com.github.havarunner.exception.ConstructorNotFound
import org.junit.internal.AssumptionViolatedException
import com.github.havarunner.TestInstanceCache._

private[havarunner] object Reflections {
  def findAnnotationRecursively(clazz: Class[_ <: Any], annotationClass: Class[_ <: Annotation]): Option[Annotation] =
    Option(clazz.getAnnotation(annotationClass)) orElse {
      Option(clazz.getSuperclass) flatMap {
        superclass => findAnnotationRecursively(superclass, annotationClass)
      }
    }

  def instantiate(implicit suiteOption: Option[HavaRunnerSuite[_]], scenarioOption: Option[AnyRef], clazz: Class[_]) = {
    val (constructor, argsOption) = resolveConstructorAndArgs(suiteOption, scenarioOption, clazz)
    constructor.setAccessible(true)
    argsOption match {
      case Some(args) => constructor.newInstance(args.toSeq.asInstanceOf[Seq[AnyRef]]:_*)
      case None       => constructor.newInstance()
    }
  }

  def withSubclasses(clazz: Class[_], accumulator: Seq[Class[_]] = Seq()): Seq[Class[_]] =
    if (clazz.getDeclaredClasses.isEmpty) {
      clazz +: accumulator
    } else {
      clazz +: clazz.getDeclaredClasses.flatMap(withSubclasses(_, accumulator))
    }

  private def resolveConstructorAndArgs(
                                         implicit suiteOption: Option[HavaRunnerSuite[_]],
                                         scenarioOption: Option[AnyRef],
                                         clazz: Class[_]
                                         ) =
    withHelpfulConstructorMissingReport {
      (suiteOption, scenarioOption) match {
        case (Some(suite), Some(scenario)) =>
          (
            clazz.getDeclaredConstructor(suite.suiteObject.getClass, scenario.getClass),
            Some(suite.suiteObject :: scenario :: Nil)
          )
        case (Some(suite), None) =>
          (
            clazz.getDeclaredConstructor(suite.suiteObject.getClass),
            Some(suite.suiteObject :: Nil)
          )
        case (None, Some(scenario)) =>
          (
            clazz.getDeclaredConstructor(scenario.getClass),
            Some(scenario :: Nil)
          )
        case (None, None) =>
          (clazz.getDeclaredConstructor(), None)
      }
    }

  private def withHelpfulConstructorMissingReport[T](op: => T)(implicit clazz: Class[_], scenario: Option[AnyRef]) =
    try {
      op
    } catch {
      case e: NoSuchMethodException =>
        throw new ConstructorNotFound(clazz, e)
    }

  def findMethods(clazz: Class[_], annotation: Class[_ <: Annotation]): Seq[Method] =
    classWithSuperclasses(clazz).flatMap(clazz =>
      clazz.getDeclaredMethods.filter(_.getAnnotation(annotation) != null)
    )

  def invoke(method: Method, testAndParameters: TestAndParameters) {
    method.setAccessible(true)
    try {
      method.invoke(fromTestInstanceCache(testAndParameters))
    } catch {
      case e: InvocationTargetException =>
        if (e.getTargetException.getClass == classOf[AssumptionViolatedException]) {
          // Tolerate AssumptionViolatedException
        } else {
          throw e
        }
    }
  }

  def classWithSuperclasses(clazz: Class[_ <: Any], superclasses: Seq[Class[_ <: Any]] = Nil): Seq[Class[_ <: Any]] =
    if (clazz.getSuperclass != null) {
      classWithSuperclasses(clazz.getSuperclass, clazz +: superclasses)
    } else {
      superclasses
    }

  def hasMethodAnnotatedWith(clazz: Class[_], annotationClass: Class[_ <: Annotation]) =
    classWithSuperclasses(clazz).
      flatMap(_.getDeclaredMethods).
      exists(_.getAnnotation(annotationClass) != null)
  }
