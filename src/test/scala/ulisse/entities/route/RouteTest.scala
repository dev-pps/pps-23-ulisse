package ulisse.entities.route

import cats.data.Chain
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.{Route, RouteError, RouteType}
import ulisse.entities.station.Station
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.utils.ValidationUtils.mkStringErrors

object RouteTest:
  type ValidationRoute = Either[RouteError, Route]

  private val departureCoordinate = Coordinate(2, 0)
  private val arrivalCoordinate   = Coordinate(0, 2)
  val railsCount: Int             = 2
  val departure: Station          = Station("Rimini", departureCoordinate, railsCount)
  val arrival: Station            = Station("Cesena", arrivalCoordinate, railsCount)
  val routeType: RouteType        = RouteType.Normal
  val pathLength: Double          = departureCoordinate.distance(arrivalCoordinate)

class RouteTest extends AnyFlatSpec with Matchers:
  import RouteTest.*
  val validateRoute: ValidationRoute = Route(departure, arrival, routeType, railsCount, pathLength)

  "create routes" should "set core parameters: typology, railsCount, path" in:
    validateRoute match
      case Left(errors) => fail(errors.mkStringErrors)
      case Right(route) =>
        route.departure mustBe departure
        route.arrival mustBe arrival
        route.typology mustBe routeType
        route.railsCount mustBe railsCount
        route.length mustBe pathLength

  "check equals routes" should "be same typology and path and the others parameters different" in:
    val equalRoute: ValidationRoute = Route(departure, arrival, routeType, railsCount - 1, pathLength + 1)
    validateRoute mustBe equalRoute

  "check different routes" should "have different departure or arrival station or typology " in:
    val stationTracks: Int                     = 2
    val newDeparture: Station                  = Station("Firenze", departureCoordinate, stationTracks)
    val newArrival: Station                    = Station("Bologna", arrivalCoordinate, stationTracks)
    val routeWithNewDeparture: ValidationRoute = Route(newDeparture, arrival, routeType, railsCount, pathLength)
    val routeWithNewArrival: ValidationRoute   = Route(departure, newArrival, routeType, railsCount, pathLength)
    val createRouteByType: RouteType => Either[RouteError, Route] = Route(departure, arrival, _, railsCount, pathLength)

    validateRoute must not be routeWithNewDeparture
    validateRoute must not be routeWithNewArrival

    RouteType.values filter (!_.canEqual(routeType)) foreach (validateRoute must not be createRouteByType(_))

  "check all fields from equal route" should "be equals" in:
    for route <- validateRoute
    yield route checkAllField route mustBe true

  "check all fields from different route" should "be different" in:
    for
      route          <- validateRoute
      differentRoute <- Route(departure, arrival, RouteType.AV, railsCount, pathLength + 100)
    yield route checkAllField differentRoute mustBe false

  "route with departure" should "change routes departure" in:
    val newDeparture: Station = Station("Firenze", departure.coordinate, 2)
    for
      route                <- validateRoute
      changeRouteDeparture <- route withDeparture newDeparture
    yield changeRouteDeparture.departure mustBe newDeparture

  "route with automatic departure" should "change routes departure" in:
    val newDeparture: Station = Station("Firenze", departure.coordinate, 2)
    for route <- validateRoute
    yield
      val newRoute = route changeAutomaticDeparture newDeparture
      newRoute.departure mustBe newDeparture
      newRoute.isValid mustBe true

  "route with arrival" should "change routes arrival" in:
    val newArrival: Station = Station("Bologna", arrival.coordinate, 2)
    for
      route              <- validateRoute
      changeRouteArrival <- route withArrival newArrival
    yield changeRouteArrival.arrival mustBe newArrival

  "route with automatic arrival" should "change routes arrival" in:
    val newArrival: Station = Station("Bologna", arrival.coordinate, 2)
    for route <- validateRoute
    yield
      val newRoute = route changeAutomaticArrival newArrival
      newRoute.arrival mustBe newArrival
      newRoute.isValid mustBe true

  "route with typology" should "change routes typology" in:
    val newTypology: RouteType = RouteType.AV
    for route <- validateRoute
    yield (route withTypology newTypology).typology mustBe newTypology

  "route with rails count" should "change routes rails count" in:
    val newRailsCount: Int = railsCount - 1
    for
      route                 <- validateRoute
      changeRouteRailsCount <- route withRailsCount newRailsCount
    yield changeRouteRailsCount.railsCount mustBe newRailsCount

  "route with length" should "change routes length" in:
    val newLength: Double = pathLength + 50.0d
    for
      route             <- validateRoute
      changeRouteLength <- route withLength newLength
    yield changeRouteLength.length mustBe newLength

  "create route with same departure and arrival" should "launch error" in:
    val error                        = Left(Chain(Routes.Errors.SameStation))
    val sameStation: ValidationRoute = Route(departure, departure, routeType, railsCount, pathLength)
    sameStation mustBe error
    validateRoute.foreach(_ withArrival departure mustBe error)
    validateRoute.foreach(_ withDeparture arrival mustBe error)

  "create route with few rails" should "launch error" in:
    val error                     = Left(Chain(Routes.Errors.FewRails))
    val newRailsCount             = 0
    val fewRails: ValidationRoute = Route(departure, arrival, routeType, newRailsCount, pathLength)
    fewRails mustBe error
    validateRoute.foreach(_ withRailsCount newRailsCount mustBe error)

  "create route with too many rails" should "launch error" in:
    val error                         = Left(Chain(Routes.Errors.TooManyRails))
    val newRailsCount                 = 3
    val tooManyRails: ValidationRoute = Route(departure, arrival, routeType, newRailsCount, pathLength)
    tooManyRails mustBe error
    validateRoute.foreach(_ withRailsCount newRailsCount mustBe error)

  "create route with too short path" should "launch error" in:
    val error                         = Left(Chain(Routes.Errors.TooShort))
    val newLength                     = pathLength - 1
    val tooShortPath: ValidationRoute = Route(departure, arrival, routeType, railsCount, newLength)
    tooShortPath mustBe error
    validateRoute.foreach(_ withLength newLength mustBe error)

  "check departure station" should "be equals" in:
    for route <- validateRoute
    yield route isDeparture departure mustBe true

  "check departure station with arrival station" should "be different" in:
    for route <- validateRoute
    yield route isDeparture arrival mustBe false

  "check arrival station" should "be equals" in:
    for route <- validateRoute
    yield route isArrival arrival mustBe true

  "check arrival station with departure station" should "be different" in:
    for route <- validateRoute
    yield route isArrival departure mustBe false

  "check right direction of route" should "be equals" in:
    for route <- validateRoute
    yield route isRightDirection (departure, arrival) mustBe true

  "check right direction of route with reverse direction" should "be different" in:
    for route <- validateRoute
    yield route isRightDirection (arrival, departure) mustBe false

  "check reverse direction of route" should "be equals" in:
    for route <- validateRoute
    yield route isReverseDirection (arrival, departure) mustBe true

  "check reverse direction of route with right direction" should "be different" in:
    for route <- validateRoute
    yield route isReverseDirection (departure, arrival) mustBe false

  "check path of route" should "be equals" in:
    for route <- validateRoute
    yield route isPath (departure, arrival) mustBe true

  "check path all combinations of stations" should "be equals" in:
    for route <- validateRoute
    yield
      route isPath (departure, arrival) mustBe true
      route isPath (arrival, departure) mustBe true
      route isPath (departure, departure) mustBe false
      route isPath (arrival, arrival) mustBe false

  "check technology of route" should "be equals" in:
    for route <- validateRoute
    yield route isTechnology RouteType.Normal.technology mustBe true

  "check technology of route with different technology" should "be different" in:
    for route <- validateRoute
    yield route isTechnology RouteType.AV.technology mustBe false

  private def mockTrainWithTechnologyMaxSpeed(maxSpeed: Int): Train =
    val train           = mock[Train]
    val trainTechnology = TrainTechnology("Test", maxSpeed, 0, 0)
    when(train.techType) thenReturn trainTechnology
    train

  "check train technology acceptable" should "be equals" in:
    val train = mockTrainWithTechnologyMaxSpeed(routeType.technology.maxSpeed - 1)
    for route <- validateRoute
    yield route acceptTrainTechnology train mustBe true

  "check train technology not acceptable" should "be different" in:
    val train = mockTrainWithTechnologyMaxSpeed(routeType.technology.maxSpeed + 1)
    for route <- validateRoute
    yield route acceptTrainTechnology train mustBe false
