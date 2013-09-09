package havarunner.exception

import java.lang.annotation.Annotation

class UnsupportedAnnotationException(val annotationClass: Class[_ <: Annotation], val annotationUser: Any)
  extends RuntimeException(
    String.format(
      "%s uses the unsupported annotation %s",
      annotationUser.toString,
      annotationClass.getName
    )
  )
