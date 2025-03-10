package ulisse.applications

import ulisse.applications.configs.{MediumRailwayConfig, RailwayBaseConfig}
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.dsl.RailwayDsl.*
import ulisse.entities.route.Routes.RouteType
import ulisse.entities.timetable.Timetables.RailInfo
import ulisse.entities.train.Trains.TrainTechnology
import ulisse.entities.train.Wagons.UseType
import ulisse.utils.Times.FluentDeclaration.h

/** Configuration for the railway. */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
object RailwayConfig:

  private val highSpeed         = TrainTechnology("AV", 300, 2.0, 1.0)
  private val normal            = TrainTechnology("Normal", 160, 1, 0.5)
  private val technologyManager = TechnologyManager(List(highSpeed, normal))

  private val initAppState: AppState  = AppState.withTechnology(technologyManager)
  private var uploadRailway: AppState = initAppState

  private val stationA = CreateStation -> "A" at (314, 263) platforms 2
  private val stationB = CreateStation -> "B" at (528, 147) platforms 2
  private val stationC = CreateStation -> "C" at (803, 219) platforms 3
  private val stationD = CreateStation -> "D" at (927, 444) platforms 3
  private val stationE = CreateStation -> "E" at (813, 768) platforms 3
  private val stationF = CreateStation -> "F" at (462, 759) platforms 4
  private val stationG = CreateStation -> "G" at (286, 514) platforms 4
  private val stationH = CreateStation -> "H" at (492, 540) platforms 4
  private val stationI = CreateStation -> "I" at (590, 365) platforms 4
  private val stationJ = CreateStation -> "J" at (725, 568) platforms 5

  private val railN400  = RailInfo(length = 400, typeRoute = RouteType.Normal)
  private val railAV400 = RailInfo(length = 400, typeRoute = RouteType.AV)

  private val routeFG = CreateRoute -> stationF -> stationG on railN400.typeRoute tracks 2 length railN400.length
  private val routeGA = CreateRoute -> stationG -> stationA on railN400.typeRoute tracks 2 length railN400.length
  private val routeAB = CreateRoute -> stationA -> stationB on railN400.typeRoute tracks 2 length railN400.length
  private val routeBC = CreateRoute -> stationB -> stationC on railN400.typeRoute tracks 2 length railN400.length
  private val routeCD = CreateRoute -> stationC -> stationD on railN400.typeRoute tracks 2 length railN400.length
  private val routeDE = CreateRoute -> stationD -> stationE on railN400.typeRoute tracks 2 length railN400.length
  private val routeEH = CreateRoute -> stationE -> stationH on railN400.typeRoute tracks 2 length railN400.length
  private val routeHF = CreateRoute -> stationH -> stationF on railAV400.typeRoute tracks 2 length railAV400.length
  private val routeHG = CreateRoute -> stationH -> stationG on railAV400.typeRoute tracks 2 length railAV400.length
  private val routeHB = CreateRoute -> stationH -> stationB on railAV400.typeRoute tracks 2 length railAV400.length
  private val routeBI = CreateRoute -> stationB -> stationI on railAV400.typeRoute tracks 2 length railAV400.length
  private val routeID = CreateRoute -> stationI -> stationD on railAV400.typeRoute tracks 2 length railAV400.length
  private val routeJE = CreateRoute -> stationJ -> stationE on railAV400.typeRoute tracks 2 length railAV400.length
  private val routeIJ = CreateRoute -> stationI -> stationJ on railAV400.typeRoute tracks 2 length railAV400.length
  private val routeIH = CreateRoute -> stationI -> stationH on railAV400.typeRoute tracks 2 length railAV400.length

  private val trainA_AV = CreateTrain -> "AV1" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainB_AV = CreateTrain -> "AV2" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainC_AV = CreateTrain -> "AV3" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainD_AV = CreateTrain -> "AV4" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainE_AV = CreateTrain -> "AV5" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainF_AV = CreateTrain -> "AV6" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainG_AV = CreateTrain -> "AV7" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainH_AV = CreateTrain -> "AV8" technology highSpeed wagon UseType.Passenger capacity 2 count 11
  private val trainN_NR = CreateTrain -> "NR4" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainO_NR = CreateTrain -> "NR5" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainP_NR = CreateTrain -> "NR6" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainQ_NR = CreateTrain -> "NR7" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainR_NR = CreateTrain -> "NR8" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainS_NR = CreateTrain -> "NR9" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainT_NR = CreateTrain -> "NR10" technology normal wagon UseType.Passenger capacity 2 count 1

  import ulisse.dsl.TimetableDSL.*
  private val table1 = trainA_AV at h(0).m(0).getOrDefault startFrom stationA thenOnRail
    railAV400 andStopIn stationB waitingForMinutes 5 thenOnRail
    railAV400 travelsTo stationC thenOnRail
    railAV400 andStopIn stationD waitingForMinutes 10 thenOnRail
    railAV400 arrivesTo stationE

  private val table2 = trainB_AV at h(1).m(0).getOrDefault startFrom stationD thenOnRail
    railAV400 andStopIn stationC waitingForMinutes 5 thenOnRail
    railAV400 travelsTo stationB thenOnRail
    railAV400 arrivesTo stationA

  private val table3 = trainC_AV at h(3).m(0).getOrDefault startFrom stationF thenOnRail
    railAV400 travelsTo stationH thenOnRail
    railN400 travelsTo stationG thenOnRail
    railAV400 arrivesTo stationA

  private val table4 = trainD_AV at h(2).m(0).getOrDefault startFrom stationA thenOnRail
    railAV400 travelsTo stationG thenOnRail
    railN400 andStopIn stationH waitingForMinutes 20 thenOnRail
    railAV400 arrivesTo stationF

  private val table5 = trainE_AV at h(2).m(0).getOrDefault startFrom stationA thenOnRail
    railAV400 travelsTo stationG thenOnRail
    railN400 travelsTo stationH thenOnRail
    railAV400 arrivesTo stationF

  private val table6 = trainF_AV at h(4).m(0).getOrDefault startFrom stationE thenOnRail
    railAV400 travelsTo stationD thenOnRail
    railAV400 andStopIn stationC waitingForMinutes 5 thenOnRail
    railAV400 travelsTo stationB thenOnRail
    railAV400 arrivesTo stationA

  private val table7 = trainG_AV at h(5).m(0).getOrDefault startFrom stationA thenOnRail
    railAV400 travelsTo stationB thenOnRail
    railAV400 travelsTo stationI thenOnRail
    railAV400 andStopIn stationJ waitingForMinutes 10 thenOnRail
    railAV400 arrivesTo stationE

  private val table8 = trainH_AV at h(6).m(0).getOrDefault startFrom stationF thenOnRail
    railAV400 travelsTo stationH thenOnRail
    railAV400 travelsTo stationI thenOnRail
    railAV400 andStopIn stationD waitingForMinutes 15 thenOnRail
    railAV400 arrivesTo stationC

  private val table9 = trainN_NR at h(2).m(30).getOrDefault startFrom stationA thenOnRail
    railN400 travelsTo stationB thenOnRail
    railN400 andStopIn stationC waitingForMinutes 5 thenOnRail
    railN400 travelsTo stationD thenOnRail
    railN400 arrivesTo stationE

  private val table10 = trainO_NR at h(3).m(30).getOrDefault startFrom stationF thenOnRail
    railN400 travelsTo stationG thenOnRail
    railN400 travelsTo stationA thenOnRail
    railN400 andStopIn stationB waitingForMinutes 10 thenOnRail
    railN400 arrivesTo stationC

  private val table11 = trainP_NR at h(4).m(30).getOrDefault startFrom stationE thenOnRail
    railN400 travelsTo stationH thenOnRail
    railN400 travelsTo stationF thenOnRail
    railN400 arrivesTo stationG

  private val table12 = trainQ_NR at h(5).m(30).getOrDefault startFrom stationJ thenOnRail
    railN400 travelsTo stationI thenOnRail
    railN400 travelsTo stationH thenOnRail
    railN400 andStopIn stationG waitingForMinutes 10 thenOnRail
    railN400 arrivesTo stationA

  uploadRailway = CreateAppState ++ uploadRailway set stationA set stationB set stationC set stationD set stationE set
    stationF set stationG set stationH set stationI set stationJ link
    routeFG link routeGA link routeAB link routeBC link routeCD link
    routeDE link routeEH link routeHF link routeHG link routeHB link
    routeBI link routeID link routeJE link routeIJ link routeIH put
    trainA_AV put trainB_AV put trainC_AV put trainD_AV put trainE_AV put
    trainF_AV put trainG_AV put trainH_AV put trainN_NR put trainO_NR put
    trainP_NR put trainQ_NR put trainR_NR put trainS_NR put trainT_NR scheduleA table1 scheduleA
    table2 scheduleA table3 scheduleA table4 scheduleA table5 scheduleA table6 scheduleA table7 scheduleA
    table8 scheduleA table9 scheduleA table10 scheduleA table11 scheduleA table12

  private val simpleRailway: AppState  = RailwayBaseConfig.railwayConfig
  private val mediumRailway: AppState  = MediumRailwayConfig.mediumRailwayConfig
  private val complexRailway: AppState = uploadRailway

  /** Get the simple railway configuration. */
  def simpleRailwayConfig: AppState = simpleRailway

  /** Get the normal railway configuration. */
  def normalRailwayConfig: AppState = mediumRailway

  /** Get the complex railway configuration. */
  def complexRailwayConfig: AppState = complexRailway
