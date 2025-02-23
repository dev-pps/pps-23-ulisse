package ulisse.utils

import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.train.TrainAgent

@SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
object DSLTEST:

//  type UPDATEDROUTE = Option[F]
//  type ROUTETOFIND = Option[F]
//  type BParam = (D, (D, ROUTETOFIND) => UPDATEDROUTE)
//  type B = BParam => UPDATEDROUTE
//  type C = BParam
//  type D = TrainAgent
//  type E = (D, ROUTETOFIND) => UPDATEDROUTE
//  type F = RouteEnvironmentElement
//
//  val updateStateInRoute: E = (agent, route) => route.flatMap(_.updateTrain(agent))
//  val foundInRoutes: B = (agent, usir) => usir(agent, agent.findInRoutes(routes))
//  extension (p1: BParam)
//    def is(p2: B): UPDATEDROUTE = p2(p1)
//
//  extension (p1: E)
//    def when(p2: D): C = (p2, p1)
//
//  updateStateInRoute when agent is foundInRoutes
//  type A = Option[F]
//  type B = D => A
//  type C = (D, F) => A
//  type D = TrainAgent
//  type E = (TrainAgent, F) => A
//  type F = RouteEnvironmentElement
//
//
//  val updateStateInRoute: E = ???
//  val agent: D = ???
//  val foundInRoutes: B = ???
//  extension (p1: C)
//    def is(p2: B): A = ???
//
//  extension (p1: E)
//    def when(p2: D): C = ???
//
//  updateStateInRoute when agent is foundInRoutes
  case class TrainAgent()
  trait TestEnv:
    def updateTrain(trainAgent: TrainAgent): Option[TestEnv]
  case class TestStation() extends TestEnv:
    override def updateTrain(trainAgent: TrainAgent): Option[TestStation] = Some(this)
  case class TestRoute() extends TestEnv:
    override def updateTrain(trainAgent: TrainAgent): Option[TestRoute] = Some(this)
//  def updateEnvironmentState: EnvironmentElementUpdater[TestRoute] =
//    (agent, envEl) => envEl.flatMap(env => env.updateTrain(agent))
  def update(p1: TrainAgent, p2: TestRoute): Option[TestRoute] =
    p2.updateTrain(p1)
  def update(p1: TrainAgent, p2: TestStation): Option[TestStation] =
    p2.updateTrain(p1)

  extension (trainAgent: TrainAgent)
    def findInRoutes(routes: Seq[TestRoute]): Option[TestRoute] =
      routes.headOption
    def findInStations(stations: Seq[TestStation]): Option[TestStation] =
      stations.headOption

  val routes = Seq(TestRoute())
  val agent  = TrainAgent()
  object EnvironmentUpdater:
    type UpdatedEnvironmentElement[EnvironmentElementWhereAgentIs] = Option[EnvironmentElementWhereAgentIs]
    type RouteWhereAgentIs                                         = Option[RouteEnvironmentElement]
    type StationWhereAgentIs                                       = Option[StationEnvironmentElement]
    type EnvironmentElementUpdater[EnvironmentElementWhereAgentIs] =
      (TrainAgent, Option[EnvironmentElementWhereAgentIs]) => UpdatedEnvironmentElement[EnvironmentElementWhereAgentIs]
    type EnvironmentElementWhereAgentIsFinder[EnvironmentElementWhereAgentIs] =
      TrainAgent => Option[EnvironmentElementWhereAgentIs]
    type UpdateRequirement[EnvironmentElementWhereAgentIs] =
      (TrainAgent, EnvironmentElementUpdater[EnvironmentElementWhereAgentIs])
    type EnvironmentElementUpdaterFunction[EnvironmentElementWhereAgentIs] =
      UpdateRequirement[EnvironmentElementWhereAgentIs] => UpdatedEnvironmentElement[EnvironmentElementWhereAgentIs]

    def updateEnvironmentState: EnvironmentElementUpdater[TestRoute] =
      (agent, envEl) => envEl.flatMap(env => env.updateTrain(agent))

    def updateEnvironmentState2: EnvironmentElementUpdater[TestStation] =
      (agent, envEl) => envEl.flatMap(env => env.updateTrain(agent))

    val foundInRoutes: EnvironmentElementUpdaterFunction[TestRoute] =
      (agent, usir) => usir(agent, agent.findInRoutes(routes))
    val foundInStations: EnvironmentElementUpdaterFunction[TestStation] =
      (agent, usir) => usir(agent, agent.findInStations(Seq(TestStation())))
    extension [EnvironmentElementWhereAgentIs <: TestEnv](p1: UpdateRequirement[EnvironmentElementWhereAgentIs])
      def is(p2: EnvironmentElementUpdaterFunction[EnvironmentElementWhereAgentIs])
          : UpdatedEnvironmentElement[EnvironmentElementWhereAgentIs] = p2(p1)

    extension [EnvironmentElementWhereAgentIs <: TestEnv](p1: EnvironmentElementUpdater[EnvironmentElementWhereAgentIs])
      def when(p2: TrainAgent): UpdateRequirement[EnvironmentElementWhereAgentIs] = (p2, p1)

    val result: UpdatedEnvironmentElement[TestRoute]    = updateEnvironmentState when agent is foundInRoutes
    val result2: UpdatedEnvironmentElement[TestStation] = updateEnvironmentState2 when agent is foundInStations
//    result and result2
    updateState:
      updateEnvironmentState when agent is foundInRoutes
      updateEnvironmentState2 when agent is foundInStations

//    def updateState(s: () => (UpdatedEnvironmentElement[TestRoute], UpdatedEnvironmentElement[TestStation])): (UpdatedEnvironmentElement[TestRoute], UpdatedEnvironmentElement[TestStation]) = s()

    def updateState(elements: UpdatedEnvironmentElement[?]*): Unit = {
      elements.foreach(println)
    }
