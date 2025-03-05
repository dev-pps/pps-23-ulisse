package ulisse.adapters.input

import ulisse.applications.managers.RouteManagers
import ulisse.applications.ports.RoutePorts
import ulisse.entities.route.Routes.{Route, RouteError, RouteType}
import ulisse.entities.station.Station

import scala.concurrent.Future

/** Adapter for the route input ports. */
trait RouteAdapter:

  /** Errors that can be generated during calls to the [[RouteAdapter]] methods. */
  type RouteAdapterError = RouteManagers.Errors | RouteError

  /** Save route from the given information, departure and arrival [[Station]], [[RouteType]], number of rails and length. */
  def save(
      oldRoute: Option[Route],
      departure: Station,
      arrival: Station,
      typology: RouteType,
      rails: Int,
      length: Double
  ): Future[Either[RouteAdapterError, List[Route]]]

  /** Delete the route. */
  def delete(
      departure: Station,
      arrival: Station,
      typology: RouteType,
      rails: Int,
      length: Double
  ): Future[Either[RouteAdapterError, List[Route]]]

/** Companion object for the [[RouteAdapter]] class. */
object RouteAdapter:

  /** Creates a [[RouteAdapter]] instance. */
  def apply(port: RoutePorts.Input): RouteAdapter = RouteAdapterImpl(port)

  private case class RouteAdapterImpl(private val port: RoutePorts.Input) extends RouteAdapter:

    private def create(
        departure: Station,
        arrival: Station,
        typology: RouteType,
        rails: Int,
        length: Double
    ): Future[Either[RouteAdapterError, List[Route]]] =
      val route = Route(departure, arrival, typology, rails, length)
      route.fold(error => Future successful Left(error), port.save)

    override def save(
        oldRoute: Option[Route],
        departure: Station,
        arrival: Station,
        typology: RouteType,
        rails: Int,
        length: Double
    ): Future[Either[RouteAdapterError, List[Route]]] =
      oldRoute.fold(create(departure, arrival, typology, rails, length))(route =>
        val newRoute = Route(departure, arrival, typology, rails, length)
        newRoute.fold(Future successful Left(_), port modify (route, _))
      )

    override def delete(
        departure: Station,
        arrival: Station,
        typology: RouteType,
        rails: Int,
        length: Double
    ): Future[Either[RouteAdapterError, List[Route]]] =
      val route = Route(departure, arrival, typology, rails, length)
      route.fold(Future successful Left(_), port.delete)
