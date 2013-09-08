package havarunner.exception

import java.lang.annotation.Annotation

class UnsupportedAnnotationException(annotationClass: Class[_ <: Annotation], annotationUser: Any)
  extends RuntimeException(
    String.format(
      "%s uses the unsupported annotation %s",
      annotationUser.toString,
      annotationClass.getName
    )
  )
