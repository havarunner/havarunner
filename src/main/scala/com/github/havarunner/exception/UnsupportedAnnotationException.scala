package com.github.havarunner.exception

import java.lang.annotation.Annotation

class UnsupportedAnnotationException(val annotationClass: Class[_ <: Annotation], annotationUser: Any, customMessage: Option[String])
  extends RuntimeException(
    String.format(
      "%s%s%s uses the unsupported annotation %s%s",
      customMessage.getOrElse(""),
      customMessage.map(_ => " (").getOrElse(""),
      annotationUser.toString,
      annotationClass.getName,
      customMessage.map(_ => ")").getOrElse("")
    )
  )
