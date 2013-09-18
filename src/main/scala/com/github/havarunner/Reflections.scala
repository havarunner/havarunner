package com.github.havarunner

import java.lang.annotation.Annotation
import java.lang.reflect.{Modifier, Method}
import scala.Some
import com.github.havarunner.exception.ConstructorNotFound

private[havarunner] object Reflections {
  def isAnnotatedWith(clazz: Class[_ <: Any], annotationClass: Class[_ <: Annotation]): Boolean =
    if (clazz.getAnnotation(annotationClass) != null) {
      true
    } else if (clazz.getSuperclass != null) {
      isAnnotatedWith(clazz.getSuperclass, annotationClass)
    } else {
      false
    }

  def instantiate(testAndParameters: TestAndParameters) =
    withHelpfulConstructorMissingReport(testAndParameters) {
      val constructorArgClasses: Seq[Class[_]] = Seq(
        testAndParameters.outerTest.map(_.testClass),
        testAndParameters.partOf.map(_.suiteObject.getClass),
        testAndParameters.scenario.map(_.getClass)
      ).flatten
      val args: Seq[_] = Seq(
        testAndParameters.outerTest.map(TestInstanceCache.fromTestInstanceCache(_)),
        testAndParameters.partOf.map(_.suiteObject),
        testAndParameters.scenario
      ).flatten
      val constructor = testAndParameters.testClass.getDeclaredConstructor(constructorArgClasses.asInstanceOf[Seq[Class[_]]]:_*)
      constructor.setAccessible(true)
      constructor.newInstance(args.asInstanceOf[Seq[AnyRef]]: _*)
    }

  def withSubclasses(clazz: Class[_], accumulator: Seq[ClassAndOuter] = Seq(), outer: Option[Class[_]] = None): Seq[ClassAndOuter] =
    if (clazz.getDeclaredClasses.isEmpty) {
      ClassAndOuter(clazz, outer) +: accumulator
    } else {
      ClassAndOuter(clazz, outer) +: clazz.getDeclaredClasses.flatMap(withSubclasses(_, accumulator, Some(clazz)))
    }

  private def withHelpfulConstructorMissingReport[T](testAndParameters: TestAndParameters)(op: => T) =
    try {
      op
    } catch {
      case e: NoSuchMethodException =>
        throw new ConstructorNotFound(testAndParameters.testClass, e)
    }

  def findMethods(clazz: Class[_], annotation: Class[_ <: Annotation]): Seq[Method] =
    classWithSuperclasses(clazz).flatMap(clazz =>
      clazz.getDeclaredMethods.filter(_.getAnnotation(annotation) != null)
    )

  def invoke(implicit method: Method, testAndParameters: TestAndParameters ) {
    method.setAccessible(true)
    method.invoke(TestInstanceCache.fromTestInstanceCache)
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
