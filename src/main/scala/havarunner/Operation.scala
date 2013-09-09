package havarunner

private[havarunner] case class Operation[T](op: () => T) {
  def andThen[T](chainedFunction: () => T): Operation[T] = new Operation({
    op()
    chainedFunction
  })

  def andThen[T](chainedOperation: Operation[T]): Operation[T] = new Operation({
    op()
    chainedOperation.op
  })

  def run: T = op()
}