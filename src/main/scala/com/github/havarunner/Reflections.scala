package com.github.havarunner

import java.lang.annotation.Annotation
import java.lang.reflect.Method

private[havarunner] object Reflections {
  def isAnnotatedWith(clazz: Class[_ <: Any], annotationClass: Class[_ <: Annotation]): Boolean = {
    if (clazz.getAnnotation(annotationClass) != null) {
      true
    } else if (clazz.getSuperclass != null) {
      isAnnotatedWith(clazz.getSuperclass, annotationClass)
    } else {
      false
    }
  }

  def findMethods(clazz: Class[_], annotation: Class[_ <: Annotation]): Seq[Method] = {
    val superclasses: Seq[Class[_ <: Any]] = classWithSuperclasses(clazz)
    superclasses.flatMap(clazz =>
      clazz.getDeclaredMethods.filter(_.getAnnotation(annotation) != null)
    )
  }

  def invoke(method: Method, testAndParameters: TestAndParameters) {
    method.setAccessible(true)
    method.invoke(testAndParameters.testInstance)
  }

  def classWithSuperclasses(clazz: Class[_ <: Any], superclasses: Seq[Class[_ <: Any]] = Nil): Seq[Class[_ <: Any]] = {
    if (clazz.getSuperclass != null) {
      classWithSuperclasses(clazz.getSuperclass, clazz +: superclasses)
    } else {
      superclasses
    }
  }

  def hasMethodAnnotatedWith(clazz: Class[_], annotationClass: Class[_ <: Annotation]) =
    classWithSuperclasses(clazz).
      flatMap(_.getDeclaredMethods).
      exists(_.getAnnotation(annotationClass) != null)
}
