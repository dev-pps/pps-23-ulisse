package ulisse.entities.simulation

import cats.{Functor, Monad}
import ulisse.entities.simulation.Environments.SimulationEnvironment

object Simulations:
  object EngineState:
    def empty(): EngineState = EngineState(false, None, None, 0, 0)

    enum Field:
      case Running, CyclesPerSecond, LastUpdate, LastDelta, ElapsedCycleTime

    given Conversion[ComparisonBuilder, Boolean] with
      def apply(builder: ComparisonBuilder): Boolean =
        builder.compare

    case class ComparisonBuilder(engines: List[EngineState], ignoredFields: Seq[Field]):
      def ignoring(fields: Field*): ComparisonBuilder =
        copy(ignoredFields = ignoredFields ++ fields)

      def considering(fields: Field*): ComparisonBuilder =
        copy(ignoredFields = Field.values.filterNot(fields.contains).toIndexedSeq)

      def andTo(nextEngine: EngineState): ComparisonBuilder =
        copy(engines = nextEngine +: engines)

      def compare: Boolean =
        val fieldsToCompare = Field.values.filterNot(ignoredFields.contains)
        engines match
          case Nil => true
          case firstEngine :: tail => tail.forall { otherEngine =>
              fieldsToCompare.forall {
                case Field.Running          => firstEngine.running == otherEngine.running
                case Field.CyclesPerSecond  => firstEngine.cyclesPerSecond == otherEngine.cyclesPerSecond
                case Field.LastUpdate       => firstEngine.lastUpdate == otherEngine.lastUpdate
                case Field.LastDelta        => firstEngine.lastDelta == otherEngine.lastDelta
                case Field.ElapsedCycleTime => firstEngine.elapsedCycleTime == otherEngine.elapsedCycleTime
              }
            }

    extension (engineState: EngineState)
      def compareTo(otherEngineState: EngineState): ComparisonBuilder =
        ComparisonBuilder(List(engineState, otherEngineState), Seq[Field]())
      def decreaseElapsedCycleTimeBy(delta: Double): EngineState =
        engineState.copy(elapsedCycleTime = engineState.elapsedCycleTime - delta)

  final case class EngineState(
      running: Boolean,
      cyclesPerSecond: Option[Int],
      lastUpdate: Option[Double],
      lastDelta: Double,
      elapsedCycleTime: Double
  ):
    def reset(): EngineState = EngineState(false, cyclesPerSecond, None, 0, 0)
    def update(currentUpdate: Double): EngineState =
      lastUpdate match
        case Some(lastUpdate) =>
          val deltaElapsed = currentUpdate - lastUpdate
          copy(
            lastDelta = deltaElapsed,
            elapsedCycleTime = elapsedCycleTime + deltaElapsed,
            lastUpdate = Some(currentUpdate)
          )
        case None =>
          println(s"First update: $currentUpdate")
          copy(lastUpdate = Some(currentUpdate))

  object SimulationData:
    def empty(): SimulationData = SimulationData(0, 0, SimulationEnvironment.empty())
    extension (simulationData: SimulationData)
      def increaseStepByOne(): SimulationData = simulationData.copy(step = simulationData.step + 1)
      def increaseSecondElapsedBy(delta: Double): SimulationData =
        simulationData.copy(secondElapsed = simulationData.secondElapsed + delta)

  final case class SimulationData(
      step: Int,
      secondElapsed: Double,
      simulationEnvironment: SimulationEnvironment
  ):
    def reset(): SimulationData =
      copy(step = 0, secondElapsed = 0, simulationEnvironment = SimulationEnvironment.empty())
