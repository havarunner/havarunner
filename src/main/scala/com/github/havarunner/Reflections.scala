package com.github.havarunner

import java.lang.annotation.Annotation
import java.lang.reflect._
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

  def instantiate(implicit instantiationParams: InstantiationParams, parseResult: ParseResult):
  Either[Exception, TestInstance] =
    resolveConstructorAndArgs.right.flatMap(constructorAndArgs => {
      val accessibleConstructor = ensureAccessible(constructorAndArgs._1)
      captureObjectInitialisationErrors {
        constructorAndArgs._2 match {
          case Nil  => accessibleConstructor.newInstance()
          case args => accessibleConstructor.newInstance(args.asInstanceOf[Seq[AnyRef]]:_*)
        }
      }
    })

  def findDeclaredClasses(clazz: Class[_], accumulator: Seq[Class[_]] = Seq()): Seq[Class[_]] =
    if (clazz.getDeclaredClasses.isEmpty) {
      clazz +: accumulator
    } else {
      clazz +: clazz.getDeclaredClasses.flatMap(findDeclaredClasses(_, accumulator))
    }
  
  type ConstructorAndArgs = Pair[Constructor[_], ConstructorArgs]
  type ConstructorArgs = Seq[Any]

  def resolveConstructorAndArgs(implicit instantiationParams: InstantiationParams, parseResult: ParseResult):
  Either[Exception, ConstructorAndArgs] = {
    def isEnclosingTest(candidate: TestAndParameters, encloser: ScenarioAndClass) =
      candidate.testClass == encloser.clazz && candidate.scenario.equals(instantiationParams.scenario)

    val enclosingInstance: Option[Either[Exception, TestInstance]] = instantiationParams.encloser map {
      encloser =>
        parseResult
          .find(isEnclosingTest(_, encloser))
          .map(instantiateTestClass(_, parseResult))
          .getOrElse {
            try {
              Right(TestInstance(encloser.clazz.newInstance())) // TODO fix and cache
            } catch {
              case e: Exception =>
                Left(e)
            }
          }
    }

    def constructorArgsForTopLevelTests: ConstructorArgs =
      (instantiationParams.partOf map SuiteCache.suiteInstance, instantiationParams.scenario) match {
        case (Some(suite), Some(scenario)) =>
          suite.suiteObject :: scenario :: Nil
        case (Some(suite), None) =>
          suite.suiteObject :: Nil
        case (None, Some(scenario)) =>
          scenario :: Nil
        case (None, None) =>
          Nil
      }

    val constructorArgsOrFailure = enclosingInstance match {
      case None                         => Right(constructorArgsForTopLevelTests)
      case Some(Right(testInstance))    =>
        Right(testInstance.instance :: Nil) // Non-static inner classes receive the enclosing object as the only constructor arg
      case Some(Left(constructorError)) => Left(constructorError)
    }

    constructorArgsOrFailure.right.flatMap(args =>
      try {
        Right(Pair(
          instantiationParams.testClass.getDeclaredConstructor(args.map(_.getClass): _*),
          args
        ))
      } catch constructorNotFoundOrGenericError
    )
  }

  def captureObjectInitialisationErrors(initialiseObject: => Any)(implicit instantiationParams: InstantiationParams): Either[Exception, TestInstance] =
    try {
      Right(TestInstance(initialiseObject))
    } catch constructorNotFoundOrGenericError


  def constructorNotFoundOrGenericError(implicit instantiationParams: InstantiationParams): PartialFunction[Throwable, Left[Exception, Nothing]] = {
    case e: NoSuchMethodException =>
      Left(new ConstructorNotFound(instantiationParams.testClass, e))
    case e: Exception =>
      Left(e)
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

  def invoke2(callParams: Pair[Method, InstantiationParams])(implicit parseResult: ParseResult) = {// TODO remove
    instantiateTestClass(callParams._2, parseResult).right.map(testInstance =>
      ensureAccessible(callParams._1).invoke(testInstance.instance)
    ).left.forall(throw _) // TODO handle exception elsewhere
  }

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
