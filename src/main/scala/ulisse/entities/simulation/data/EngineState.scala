package ulisse.entities.simulation.data

import ulisse.dsl.comparison.FieldsComparators.{Field, FieldComparator}

object EngineState:
  def empty(): EngineState = EngineState(false, None, 0, None, 0, 0)

  given FieldComparator[EngineStateField, EngineState] with
    def compare(engines: List[EngineState], ignoredFields: Seq[EngineStateField]): Boolean =
      val fieldsToCompare = EngineStateField.values.filterNot(ignoredFields.contains)
      engines match
        case firstEngine :: tail => tail.forall: otherEngine =>
            fieldsToCompare.forall:
              case EngineStateField.Running          => firstEngine.running == otherEngine.running
              case EngineStateField.CyclesPerSecond  => firstEngine.cyclesPerSecond == otherEngine.cyclesPerSecond
              case EngineStateField.LastUpdate       => firstEngine.lastUpdate == otherEngine.lastUpdate
              case EngineStateField.LastDelta        => firstEngine.lastDelta == otherEngine.lastDelta
              case EngineStateField.ElapsedCycleTime => firstEngine.elapsedCycleTime == otherEngine.elapsedCycleTime
        case _ => false

  enum EngineStateField extends Field[EngineStateField, EngineState]:
    case Running, CyclesPerSecond, LastUpdate, LastDelta, ElapsedCycleTime
    def values: Seq[EngineStateField] = EngineStateField.values.toSeq

  extension (engineState: EngineState)
    def decreaseElapsedCycleTimeBy(delta: Double): EngineState =
      engineState.copy(elapsedCycleTime = engineState.elapsedCycleTime - delta)

final case class EngineState(
    running: Boolean,
    cyclesPerSecond: Option[Int],
    stepSize: Int, // TODO check positive (non-negative?)
    lastUpdate: Option[Double],
    lastDelta: Double,
    elapsedCycleTime: Double
):
  def reset(): EngineState = EngineState(false, cyclesPerSecond, stepSize, None, 0, 0)

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
