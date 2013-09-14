package com.github.havarunner

import java.lang.annotation.Annotation
import java.lang.reflect.{Member, Constructor, Method}
import com.github.havarunner.Reflections._
import scala.{Array, Some}
import com.github.havarunner.HavaRunnerSuite._
import com.github.havarunner.exception.ConstructorNotFound
import scala.collection.mutable

private[havarunner] object Reflections {
  def isAnnotatedWith(clazz: Class[_ <: Any], annotationClass: Class[_ <: Annotation]): Boolean =
    if (clazz.getAnnotation(annotationClass) != null) {
      true
    } else if (clazz.getSuperclass != null) {
      isAnnotatedWith(clazz.getSuperclass, annotationClass)
    } else {
      false
    }

  def instantiate(implicit suiteOption: Option[HavaRunnerSuite[_]], scenarioOption: Option[AnyRef], clazz: Class[_]) = {
    val (constructor, argsOption) = resolveConstructorAndArgs(suiteOption, scenarioOption, clazz)
    constructor.setAccessible(true)
    argsOption match {
      case Some(args) => constructor.newInstance(args.toSeq.asInstanceOf[Seq[AnyRef]]:_*)
      case None       => constructor.newInstance()
    }
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
    method.invoke(testAndParameters.testInstance)
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
