package ulisse.adapters.input

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.adapters.input.RouteAdapter.{Errors, RouteCreationInfo}
import ulisse.applications.managers.RouteManagers
import ulisse.applications.ports.RoutePorts
import ulisse.entities.route.Routes
import ulisse.entities.route.Routes.{toRouteTypeOption, Route}
import ulisse.entities.station.Station
import ulisse.utils.Errors.ErrorMessage

import scala.concurrent.{ExecutionContext, Future}

/** Adapter for the route input ports. */
trait RouteAdapter:

  /** Errors that can be generated during calls to the [[RouteAdapter]] methods. */
  type RouteAdapterError = NonEmptyChain[Errors | Routes.Errors | RouteManagers.Errors]

  /** The return type of the [[RouteAdapter]] methods. */
  type Value = Either[RouteAdapterError, List[Route]]

  /** Save the route from the given [[RouteCreationInfo]] if oldRoute is None a new route is created, otherwise the old route is updated. */
  def save(oldRoute: Option[Route], data: RouteCreationInfo)(using ec: ExecutionContext): Future[Value]

  /** Delete the route from the given [[RouteCreationInfo]]. */
  def delete(data: RouteCreationInfo)(using ec: ExecutionContext): Future[Value]

/** Companion object for the [[RouteAdapter]] class. */
object RouteAdapter:

  /** Errors that can be generated during calls to the [[RouteAdapter]] methods. */
  enum Errors(val text: String) extends ErrorMessage(text):
    /** Not choose departure. */
    case NotChooseDeparture extends Errors("Not choose departure")

    /** Not choose arrival. */
    case NotChooseArrival extends Errors("Not choose arrival")

    /** Invalid route type. */
    case InvalidRouteType extends Errors("Invalid route type")

    /** Invalid rails count. */
    case InvalidRailsCount extends Errors("Invalid rails count")

    /** Invalid route length. */
    case InvalidRouteLength extends Errors("Invalid route length")

  /** Information required to create a new route. */
  case class RouteCreationInfo(
      departure: Option[Station],
      arrival: Option[Station],
      typology: String,
      rails: String,
      length: String
  )

  /** Extension methods for the [[Route]] class. */
  extension (route: Route)
    /** Converts the route to a [[RouteCreationInfo]] instance. */
    def toCreationInfo: RouteCreationInfo =
      RouteCreationInfo(
        route.departure.some,
        route.arrival.some,
        route.typology.toString,
        route.railsCount.toString,
        route.length.toString
      )

  /** Creates a [[RouteAdapter]] instance. */
  def apply(port: RoutePorts.Input): RouteAdapter = RouteAdapterImpl(port)

  private case class RouteAdapterImpl(private val port: RoutePorts.Input) extends RouteAdapter:

    private def create(routeData: RouteCreationInfo): Either[RouteAdapterError, Route] =
      val RouteCreationInfo(departure, arrival, typology, rails, length) = routeData
      (
        departure toValidNec Errors.NotChooseDeparture,
        arrival toValidNec Errors.NotChooseArrival,
        typology.toRouteTypeOption toValidNec Errors.InvalidRouteType,
        rails.toIntOption toValidNec Errors.InvalidRailsCount,
        length.toDoubleOption toValidNec Errors.InvalidRouteLength
      ).mapN((_, _, _, _, _)).toEither flatMap (Route(_, _, _, _, _))

    override def save(oldRoute: Option[Route], data: RouteCreationInfo)(using ec: ExecutionContext): Future[Value] =
      oldRoute.fold(create(data) onRight port.save)(route => create(data) onRight (port modify (route, _)))

    override def delete(data: RouteCreationInfo)(using ec: ExecutionContext): Future[Value] =
      create(data) onRight port.delete

    extension (creation: Either[RouteAdapterError, Route])
      private def onRight(fun: Route => Future[Either[RouteManagers.Errors, List[Route]]])(using
          ec: ExecutionContext
      ): Future[Value] = creation fold (Future successful Left(_), fun(_) map (_ leftMap NonEmptyChain.one))
