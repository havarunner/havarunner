package havarunner.exception

class ScenarioConstructorNotFound(clazz: Class[_], scenario: Option[AnyRef]) extends RuntimeException(
  String.format(
    "Class %s is missing the required scenario constructor %s(%s)",
    clazz.getSimpleName,
    clazz.getSimpleName,
    scenario.get.getClass
  )
)
