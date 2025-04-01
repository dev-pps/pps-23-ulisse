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
object RailwayExamplesConfig:

  private val highSpeed         = TrainTechnology("AV", 300, 2.0, 1.0)
  private val normal            = TrainTechnology("Normal", 160, 1, 0.5)
  private val technologyManager = TechnologyManager(List(highSpeed, normal))

  private val initAppState: AppState   = AppState.withTechnology(technologyManager)
  private var examplesConfig: AppState = initAppState
  private val railN400                 = RailInfo(length = 400, typeRoute = RouteType.Normal)
  private val railAV400                = RailInfo(length = 400, typeRoute = RouteType.AV)

  private val stationA  = CreateStation -> "A" at (300, 200) platforms 2
  private val stationB  = CreateStation -> "B" at (300, 500) platforms 2
  private val trainA_AV = CreateTrain   -> "AV1" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainB_NR = CreateTrain   -> "NR1" technology normal wagon UseType.Passenger capacity 2 count 1
  private val routeAB   = CreateRoute   -> stationA -> stationB on railN400.typeRoute tracks 2 length railN400.length

  private val stationC  = CreateStation -> "C" at (500, 200) platforms 2
  private val stationD  = CreateStation -> "D" at (500, 500) platforms 2
  private val trainC_AV = CreateTrain   -> "AV2" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainD_NR = CreateTrain   -> "NR2" technology normal wagon UseType.Passenger capacity 2 count 1
  private val routeCD   = CreateRoute   -> stationC -> stationD on railN400.typeRoute tracks 1 length railN400.length

  private val stationE  = CreateStation -> "E" at (700, 200) platforms 1
  private val stationF  = CreateStation -> "F" at (700, 500) platforms 1
  private val trainE_AV = CreateTrain   -> "AV3" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainF_NR = CreateTrain   -> "NR3" technology normal wagon UseType.Passenger capacity 2 count 1
  private val routeEF   = CreateRoute   -> stationE -> stationF on railN400.typeRoute tracks 1 length railN400.length

  private val stationG  = CreateStation -> "G" at (900, 200) platforms 2
  private val stationH  = CreateStation -> "H" at (900, 500) platforms 2
  private val trainG_AV = CreateTrain   -> "AV4" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainH_NR = CreateTrain   -> "NR4" technology normal wagon UseType.Passenger capacity 2 count 1
  private val routeGH   = CreateRoute   -> stationG -> stationH on railAV400.typeRoute tracks 2 length railAV400.length
  import ulisse.dsl.TimetableDSL.*

  private val table1 = trainA_AV at h(0).m(0).getOrDefault startFrom stationA thenOnRail
    railN400 arrivesTo stationB
  private val table2 = trainB_NR at h(0).m(0).getOrDefault startFrom stationB thenOnRail
    railN400 arrivesTo stationA

  private val table3 = trainC_AV at h(0).m(0).getOrDefault startFrom stationC thenOnRail
    railN400 arrivesTo stationD
  private val table4 = trainD_NR at h(0).m(0).getOrDefault startFrom stationD thenOnRail
    railN400 arrivesTo stationC

  private val table5 = trainE_AV at h(0).m(0).getOrDefault startFrom stationE thenOnRail
    railN400 arrivesTo stationF
  private val table6 = trainF_NR at h(0).m(0).getOrDefault startFrom stationF thenOnRail
    railN400 arrivesTo stationE

  private val table7 = trainG_AV at h(0).m(0).getOrDefault startFrom stationG thenOnRail
    railAV400 arrivesTo stationH
  private val table8 = trainH_NR at h(0).m(0).getOrDefault startFrom stationH thenOnRail
    railAV400 arrivesTo stationG

  examplesConfig =
    CreateAppState || examplesConfig set stationA set stationB set stationC set stationD set stationE set stationF set stationG set stationH link routeAB link routeCD link routeEF link routeGH put trainA_AV put trainB_NR put trainC_AV put trainD_NR put trainE_AV put trainF_NR put trainG_AV put trainH_NR scheduleA table1 scheduleA table2 scheduleA table3 scheduleA table4 scheduleA table5 scheduleA table6 scheduleA table7 scheduleA table8

  def exampleRailwayConfig: AppState = examplesConfig
