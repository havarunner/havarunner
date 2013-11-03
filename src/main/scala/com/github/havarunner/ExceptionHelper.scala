package com.github.havarunner

import org.junit.runner.Description
import org.junit.runner.notification.{Failure, RunNotifier}
import com.github.havarunner.exception.MultipleInvalidations

private[havarunner] object ExceptionHelper {
  def reportFailure(invalidationReports: Seq[_<:Exception])(implicit description: Description, notifier: RunNotifier) {
    val exception =
      if (invalidationReports.length == 1) {
        invalidationReports.head
      } else {
        new MultipleInvalidations(invalidationReports)
      }
    notifier fireTestFailure new Failure(description, exception)
  }
}
