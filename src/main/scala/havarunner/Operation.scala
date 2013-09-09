package havarunner

private[havarunner] case class Operation[T](op: () => T) {
  def andThen[T](chainedOperation: Operation[T]): Operation[T] = new Operation(
    () => {
      op()
      chainedOperation.run
    }
  )

  def run: T = op()
}