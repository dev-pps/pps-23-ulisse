package ulisse.applications.useCases

import scala.concurrent.Promise

/** Contains utility methods for services. */
object Services:

  /** Function to update a manager and return the updated manager. */
  def updateManager[Error, Value, Manager](
      promise: Promise[Either[Error, Value]],
      routeManager: Manager,
      updatedManager: Either[Error, Manager],
      values: Manager => Value
  ): Manager =
    updatedManager match
      case Left(error)       => promise.success(Left(error)); routeManager
      case Right(newManager) => promise.success(Right(values(newManager))); newManager
