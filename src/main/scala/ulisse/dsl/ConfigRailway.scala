package ulisse.dsl

import ulisse.applications.AppState
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.dsl.RailwayDsl.*
import ulisse.entities.route.Routes.RouteType
import ulisse.entities.train.Trains.TrainTechnology
import ulisse.entities.train.Wagons.UseType

/** Configuration for the railway. */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
object ConfigRailway:

  private val highSpeed         = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val normal            = TrainTechnology("Normal", 100, 0.5, 0.25)
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

  private val routeA = CreateRoute -> stationF -> stationG on RouteType.Normal tracks 2 length 400
  private val routeB = CreateRoute -> stationG -> stationA on RouteType.Normal tracks 2 length 400
  private val routeC = CreateRoute -> stationA -> stationB on RouteType.Normal tracks 2 length 400
  private val routeD = CreateRoute -> stationB -> stationC on RouteType.Normal tracks 2 length 400
  private val routeE = CreateRoute -> stationC -> stationD on RouteType.Normal tracks 2 length 400
  private val routeF = CreateRoute -> stationD -> stationE on RouteType.Normal tracks 2 length 400
  private val routeG = CreateRoute -> stationE -> stationH on RouteType.Normal tracks 2 length 400
  private val routeH = CreateRoute -> stationH -> stationF on RouteType.AV tracks 2 length 400
  private val routeI = CreateRoute -> stationH -> stationG on RouteType.AV tracks 2 length 400
  private val routeL = CreateRoute -> stationH -> stationB on RouteType.AV tracks 2 length 400
  private val routeM = CreateRoute -> stationB -> stationI on RouteType.AV tracks 2 length 400
  private val routeN = CreateRoute -> stationI -> stationD on RouteType.AV tracks 2 length 400
  private val routeO = CreateRoute -> stationJ -> stationE on RouteType.AV tracks 2 length 400
  private val routeP = CreateRoute -> stationI -> stationJ on RouteType.AV tracks 2 length 400
  private val routeQ = CreateRoute -> stationI -> stationH on RouteType.AV tracks 2 length 400

  private val trainA_AV = CreateTrain -> "AV1" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainB_AV = CreateTrain -> "AV2" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainC_AV = CreateTrain -> "AV3" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainD_AV = CreateTrain -> "AV4" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainE_AV = CreateTrain -> "AV5" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainF_AV = CreateTrain -> "AV6" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainG_AV = CreateTrain -> "AV7" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainH_AV = CreateTrain -> "AV8" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainI_AV = CreateTrain -> "AV9" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainJ_AV = CreateTrain -> "AV10" technology highSpeed wagon UseType.Passenger capacity 2 count 1
  private val trainK_NR = CreateTrain -> "NR1" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainL_NR = CreateTrain -> "NR2" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainM_NR = CreateTrain -> "NR3" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainN_NR = CreateTrain -> "NR4" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainO_NR = CreateTrain -> "NR5" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainP_NR = CreateTrain -> "NR6" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainQ_NR = CreateTrain -> "NR7" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainR_NR = CreateTrain -> "NR8" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainS_NR = CreateTrain -> "NR9" technology normal wagon UseType.Passenger capacity 2 count 1
  private val trainT_NR = CreateTrain -> "NR10" technology normal wagon UseType.Passenger capacity 2 count 1

  uploadRailway = CreateAppState ++ uploadRailway set stationA set stationB set stationC set stationD set stationE
  uploadRailway = CreateAppState ++ uploadRailway set stationF set stationG set stationH set stationI set stationJ
  uploadRailway = CreateAppState ++ uploadRailway link routeA link routeB link routeC link routeD link routeE
  uploadRailway = CreateAppState ++ uploadRailway link routeF link routeG link routeH link routeI link routeL
  uploadRailway = CreateAppState ++ uploadRailway link routeM link routeN link routeO link routeP link routeQ
  uploadRailway = CreateAppState ++ uploadRailway put trainA_AV put trainB_AV put trainC_AV put trainD_AV put trainE_AV
  uploadRailway = CreateAppState ++ uploadRailway put trainF_AV put trainG_AV put trainH_AV put trainI_AV put trainJ_AV
  uploadRailway = CreateAppState ++ uploadRailway put trainK_NR put trainL_NR put trainM_NR put trainN_NR put trainO_NR
  uploadRailway = CreateAppState ++ uploadRailway put trainP_NR put trainQ_NR put trainR_NR put trainS_NR put trainT_NR

  private var simpleRailway: AppState  = uploadRailway
  private var mediumRailway: AppState  = uploadRailway
  private var complexRailway: AppState = uploadRailway

  def simpleRailwayConfig: AppState  = simpleRailway
  def mediumRailwayConfig: AppState  = mediumRailway
  def complexRailwayConfig: AppState = complexRailway
