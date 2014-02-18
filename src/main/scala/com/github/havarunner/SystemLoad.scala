package com.github.havarunner

import scala.io.Source

private[havarunner] object SystemLoad {

  val osSpecificUptimeRegexes =
    ".*?load average: (\\d+\\.\\d+), \\d+\\.\\d+, \\d+\\.\\d+".r :: // RHEL 6
    ".*?load averages: (\\d+\\.\\d+) \\d+\\.\\d+ \\d+\\.\\d+".r  :: // OS X 10.8
    Nil

  def querySystemLoad: Int = {
    val uptimeOutputOption = callUptime.flatMap(_.getLines().toSeq.headOption)
    val loadTimeOptions: Option[List[Int]] = uptimeOutputOption.map(uptimeOutput =>
      osSpecificUptimeRegexes.flatMap(UptimeRegex =>
        uptimeOutput match {
          case UptimeRegex(shortTermLoadAverage) => Some(shortTermLoadAverage.toDouble.round.toInt)
          case _ => None
        }
      )
    )
    loadTimeOptions
      .flatMap(x => x.headOption)
      .getOrElse(0) // If we are unable to determine the system load, assume that it is zero
  }

  def callUptime =
    try {
      Some(Source.fromInputStream(Runtime.getRuntime.exec("uptime").getInputStream, "utf-8"))
    } catch {
      case e: Throwable => None
    }
}
