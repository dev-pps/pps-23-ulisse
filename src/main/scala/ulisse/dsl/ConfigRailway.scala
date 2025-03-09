package ulisse.dsl

import ulisse.applications.AppState
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.dsl.RailwayDsl.*
import ulisse.entities.train.Trains.TrainTechnology
import ulisse.entities.train.Wagons.UseType

/** Configuration for the railway. */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
object ConfigRailway:

  private val highSpeed         = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val normal            = TrainTechnology("Normal", 100, 0.5, 0.25)
  private val technologyManager = TechnologyManager(List(highSpeed, normal))

  private val initAppState: AppState   = AppState.withTechnology(technologyManager)
  private var simpleRailway: AppState  = initAppState
  private var mediumRailway: AppState  = initAppState
  private var complexRailway: AppState = initAppState

  private val stationA = CreateStation -> "A" at (409, 188) platforms 2
  private val stationB = CreateStation -> "B" at (758, 377) platforms 2
  private val stationC = CreateStation -> "C" at (505, 672) platforms 3
  private val stationD = CreateStation -> "D" at (409, 188) platforms 3
  private val stationE = CreateStation -> "E" at (758, 377) platforms 3
  private val stationF = CreateStation -> "F" at (505, 672) platforms 4
  private val stationG = CreateStation -> "G" at (409, 188) platforms 4
  private val stationH = CreateStation -> "H" at (758, 377) platforms 4
  private val stationI = CreateStation -> "I" at (505, 672) platforms 4
  private val stationJ = CreateStation -> "J" at (409, 188) platforms 5

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
