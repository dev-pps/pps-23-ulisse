package ulisse.entities.train

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.train.TrainAgentTest.{train3905, trainAgent3905}
import ulisse.entities.train.TrainAgents.{
  TrainAgent,
  TrainPerceptionInRoute,
  TrainPerceptionInStation,
  TrainRouteInfo,
  TrainStationInfo
}
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.route.Routes.RouteType.AV
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.train.MotionDatas.{emptyMotionData, MotionData}
import ulisse.entities.train.TrainAgents.TrainAgent.TrainStates
import ulisse.entities.train.TrainAgents.TrainAgent.TrainStates.{Running, Stopped}

object TrainAgentTest:
  val defaultTechnology     = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  val defaultWagon          = Wagon(UseType.Passenger, 50)
  val defaultWagonNumber    = 5
  val normalTrainTechnology = TrainTechnology("Normal", 100, 0.5, 0.25)
  val normalTrain           = Train("3908", normalTrainTechnology, defaultWagon, defaultWagonNumber)
  val train3905             = makeTrain("3905")
  val train3906             = makeTrain("3906")
  val train3907             = makeTrain("3907")

  val normalTrainAgent = makeTrainAgent(normalTrain)
  val trainAgent3905   = makeTrainAgent(train3905)
  val trainAgent3906   = makeTrainAgent(train3906)
  val trainAgent3907   = makeTrainAgent(train3907)

  def makeTrain(name: String): Train =
    Train(name, defaultTechnology, defaultWagon, defaultWagonNumber)

  def makeTrainAgent(train: Train): TrainAgent =
    TrainAgent(train)

class TrainAgentTest extends AnyWordSpec with Matchers:

  "TrainAgent" when:
    "created" should:
      "have the same train info" in:
        trainAgent3905.name shouldBe train3905.name
        trainAgent3905.techType shouldBe train3905.techType
        trainAgent3905.wagon shouldBe train3905.wagon
        trainAgent3905.length shouldBe train3905.length
        trainAgent3905.lengthSize shouldBe train3905.lengthSize
        trainAgent3905.maxSpeed shouldBe train3905.maxSpeed
        trainAgent3905.capacity shouldBe train3905.capacity

      "have a distance travelled of 0" in:
        trainAgent3905.motionData.distanceTravelled shouldBe 0

      "be in state Stopped and have no speed and acceleration" in:
        val expectedMotionData = MotionData(distanceTravelled = 0.0, speed = 0.0, acceleration = 0.0)
        trainAgent3905.state match
          case TrainStates.Stopped(motionData) =>
            motionData.distanceTravelled shouldBe expectedMotionData.distanceTravelled
          case _ => fail()

      "be initialized with some state" in:
        val initialDistance       = 0.0
        val stoppedTrain3905Agent = TrainAgent.withInitialState(train3905, Stopped(emptyMotionData))
        stoppedTrain3905Agent.state shouldBe Stopped(emptyMotionData)
        val runningState          = Running(emptyMotionData)
        val runningTrain3905Agent = TrainAgent.withInitialState(train3905, runningState)
        runningTrain3905Agent.state shouldBe runningState

    "start from station" should:
      "travel 1 km after 12 seconds" in:
        import ulisse.entities.simulation.environments.railwayEnvironment.PerceptionProvider.given
        val dt          = 12
        val stationPerc = TrainPerceptionInStation(TrainStationInfo(hasToMove = true, routeTrackIsFree = true))
        val mockEnv     = mock[RailwayEnvironment]
        when(mockEnv.perceptionFor(trainAgent3905)).thenReturn(Some(stationPerc))
        val updatedAgent               = trainAgent3905.doStep(dt, mockEnv)
        val expectedKilometerTravelled = 1.0
        val tolerance                  = 0.05
        updatedAgent match
          case agent: TrainAgent => agent.state match
              case TrainStates.Running(motionData) =>
                motionData.distanceTravelled shouldBe expectedKilometerTravelled +- tolerance
              case _ => fail()
          case _ => fail()

//    "is running on route" should:
//      "stop when reach route length" in:
//        import ulisse.entities.simulation.environments.railwayEnvironment.PerceptionProvider.given
//        val mockEnv = mock[RailwayEnvironment]
//        when(mockEnv.perceptionFor(runningTrainAgent)).thenReturn(Some(routePerc))
//        when(mockEnv.perceptionFor(runningTrainAgent)).thenReturn(Some(stationPerc))
//        val partialMotionData =
//          MotionData(travelledDistance3Km, speed = train3905.maxSpeed, acceleration = 0.0)
//        val trainRouteInfo    = TrainRouteInfo(TypeRoute.AV, routeLengthKm, None, true)
//        val routePerc         = TrainPerceptionInRoute(trainRouteInfo)
//
//        val agentReachStation = runningTrainAgent.doStep(timeToTravelRouteLength, mockEnv)
//        agentReachStation.motionData.distanceTravelled shouldBe expectedDistanceTravelledKm
//
//        val stationEnvMock = mock[RailwayEnvironment]
//        val stationPerc    = TrainPerceptionInStation(TrainStationInfo(hasToMove = true, routeTrackIsFree = true))
//
//        agentReachStation.doStep(timeToTravelRouteLength + 1, stationEnvMock).state match
//          case Stopped(md) => md.distanceTravelled shouldBe 0.0
//          case Running(_)  => fail()

    "distance is updated" should:
      "be updated correctly" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(10)
        updatedTrainAgent3905.motionData.distanceTravelled shouldBe 10
        updatedTrainAgent3905.updateDistanceTravelled(5).motionData.distanceTravelled shouldBe 15

      "be set to 0 when reset" in:
        trainAgent3905.updateDistanceTravelled(20).resetDistanceTravelled.motionData.distanceTravelled shouldBe 0

      "be at least 0" in:
        trainAgent3905.updateDistanceTravelled(10).updateDistanceTravelled(-15).motionData.distanceTravelled shouldBe 0
