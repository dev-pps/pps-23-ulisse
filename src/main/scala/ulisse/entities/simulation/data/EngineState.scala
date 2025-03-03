package ulisse.entities.simulation.data

object EngineState:
  def empty(): EngineState = EngineState(false, None, 0, None, 0, 0)

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
