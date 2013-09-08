package havarunner.exception

import havarunner.HavaRunner

class CodingConventionException(msg: String) extends RuntimeException(
  String.format(
    "%s (%s is strict about coding conventions, because its authors believe they help writing better software.)",
    msg, classOf[HavaRunner].getSimpleName
  )
)