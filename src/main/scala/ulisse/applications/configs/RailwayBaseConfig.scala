package ulisse.applications.configs

import ulisse.applications.AppState
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.dsl.RailwayDsl.*
import ulisse.entities.route.Routes.RouteType
import ulisse.entities.timetable.Timetables.RailInfo
import ulisse.entities.train.Trains.TrainTechnology
import ulisse.entities.train.Wagons.UseType
import ulisse.utils.Times.FluentDeclaration.h

/** Base configuration for the railway. */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
object RailwayBaseConfig:

  private val highSpeed         = TrainTechnology("AV", 300, 2.0, 1.0)
  private val normal            = TrainTechnology("Normal", 160, 1, 0.5)
  private val technologyManager = TechnologyManager(List(highSpeed, normal))

  private val initAppState: AppState  = AppState.withTechnology(technologyManager)
  private var uploadRailway: AppState = initAppState

  private val stationA = CreateStation -> "A" at (314, 263) platforms 2
  private val stationB = CreateStation -> "B" at (528, 147) platforms 2
  private val stationC = CreateStation -> "C" at (803, 219) platforms 3

  private val railN400 = RailInfo(length = 400, typeRoute = RouteType.Normal)

  private val routeAB = CreateRoute -> stationA -> stationB on railN400.typeRoute tracks 2 length railN400.length
  private val routeBC = CreateRoute -> stationB -> stationC on railN400.typeRoute tracks 2 length railN400.length

  private val trainA_AV = CreateTrain -> "AV1" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainB_NR = CreateTrain -> "NR1" technology normal wagon UseType.Passenger capacity 2 count 1

  import ulisse.dsl.TimetableDSL.*
  private val table1 = trainA_AV at h(0).m(0).getOrDefault startFrom stationA thenOnRail
    railN400 stopsIn stationB waitingForMinutes 5 thenOnRail
    railN400 arrivesTo stationC

  private val table2 = trainB_NR at h(1).m(0).getOrDefault startFrom stationC thenOnRail
    railN400 travelsTo stationB thenOnRail
    railN400 arrivesTo stationA

  uploadRailway = CreateAppState || uploadRailway set stationA set stationB set stationC link
    routeAB link routeBC put trainA_AV put trainB_NR scheduleA table1 scheduleA table2

  def railwayConfig: AppState = uploadRailway
