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
  private var mediumRailway: AppState = initAppState

  private val stationA = CreateStation -> "A" at (314, 263) platforms 2
  private val stationB = CreateStation -> "B" at (528, 147) platforms 2
  private val stationC = CreateStation -> "C" at (803, 219) platforms 3
  private val stationD = CreateStation -> "D" at (927, 444) platforms 3

  private val stationE = CreateStation -> "E" at (700, 500) platforms 2
  private val stationF = CreateStation -> "F" at (600, 600) platforms 3

  private val railN400  = RailInfo(length = 400, typeRoute = RouteType.Normal)
  private val railAV400 = RailInfo(length = 400, typeRoute = RouteType.AV)

  private val routeAB = CreateRoute -> stationA -> stationB on railN400.typeRoute tracks 2 length railN400.length
  private val routeBC = CreateRoute -> stationB -> stationC on railAV400.typeRoute tracks 2 length railAV400.length
  private val routeCD = CreateRoute -> stationC -> stationD on railAV400.typeRoute tracks 2 length railAV400.length
  private val routeDE = CreateRoute -> stationD -> stationE on railAV400.typeRoute tracks 2 length railAV400.length
  private val routeEF = CreateRoute -> stationE -> stationF on railN400.typeRoute tracks 2 length railN400.length

  private val trainA_AV = CreateTrain -> "AV1" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainB_AV = CreateTrain -> "AV2" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainC_AV = CreateTrain -> "AV3" technology highSpeed wagon UseType.Passenger capacity 3 count 1
  private val trainD_AV = CreateTrain -> "AV4" technology highSpeed wagon UseType.Passenger capacity 2 count 1

  import ulisse.dsl.TimetableDSL.*

  private val table1 = trainA_AV at h(0).m(0).getOrDefault startFrom stationA thenOnRail
    railAV400 travelsTo stationB thenOnRail
    railAV400 arrivesTo stationC
  private val table2 = trainD_AV at h(1).m(0).getOrDefault startFrom stationA thenOnRail
    railN400 travelsTo stationB thenOnRail
    railN400 arrivesTo stationC
  private val table3 = trainB_AV at h(2).m(0).getOrDefault startFrom stationC thenOnRail
    railAV400 travelsTo stationB thenOnRail
    railAV400 arrivesTo stationA

  private val table4 = trainC_AV at h(3).m(0).getOrDefault startFrom stationD thenOnRail
    railAV400 travelsTo stationE thenOnRail
    railN400 arrivesTo stationF
  private val table5 = trainB_AV at h(4).m(0).getOrDefault startFrom stationF thenOnRail
    railN400 travelsTo stationE thenOnRail
    railAV400 arrivesTo stationD
  private val table6 = trainA_AV at h(6).m(0).getOrDefault startFrom stationB thenOnRail
    railAV400 travelsTo stationC thenOnRail
    railAV400 arrivesTo stationD

  private val table10 = trainC_AV at h(5).m(0).getOrDefault startFrom stationE thenOnRail
    railN400 travelsTo stationF thenOnRail
    railN400 arrivesTo stationD

  mediumRailway =
    CreateAppState || mediumRailway set stationA set stationB set stationC set stationD set stationE set stationF link routeAB link routeBC link routeCD link routeDE link routeEF put trainA_AV put trainB_AV put trainD_AV put trainC_AV scheduleA table1 scheduleA table2 scheduleA table3 scheduleA table4 scheduleA table5 scheduleA table6 scheduleA table10

  def railwayConfig: AppState = mediumRailway
