package ulisse.entities.simulation.data

import ulisse.dsl.comparison.FieldsComparators.{Field, FieldComparator}

/** Represent the state of the simulation engine */
trait EngineState:

  /** The last update time */
  def lastUpdate: Option[Long]

  /** The last delta time */
  def lastDelta: Long

  /** The elapsed cycle time */
  def elapsedCycleTime: Long

  /** The elapsed cycle time increment */
  def updateElapsedCycleTime(delta: Long): EngineState

  /** function to update the engine state */
  def update(currentUpdate: Long): EngineState

/** Factory for [[EngineState]] instances */
object EngineState:
  /** Default last update */
  val defaultLastUpdate: Option[Long] = None

  /** Default last delta */
  val defaultLastDelta: Long = 0

  /** Default elapsed cycle time */
  val defaultElapsedCycleTime: Long = 0

  /** Create a new Engine state, allowing only non negative lastUpdate and positive lastDelta and elapsedCycleTime, otherwise substitute the wrong values with defaults */
  def apply(lastUpdate: Option[Long], lastDelta: Long, elapsedCycleTime: Long): EngineState =
    EngineStateImpl(
      lastUpdate.filter(_ > 0),
      math.max(defaultLastDelta, lastDelta),
      math.max(defaultElapsedCycleTime, elapsedCycleTime)
    )

  /** Create a new Engine state with default values */
  def empty(): EngineState = EngineState(defaultLastUpdate, defaultLastDelta, defaultElapsedCycleTime)

  /** Create a FieldComparator for EngineState */
  given FieldComparator[EngineStateField, EngineState] with
    def fields: Seq[EngineStateField] = EngineStateField.values.toSeq
    def _compare(firstEngineState: EngineState, otherEngineState: EngineState, field: EngineStateField): Boolean =
      field match
        case EngineStateField.LastUpdate       => firstEngineState.lastUpdate == otherEngineState.lastUpdate
        case EngineStateField.LastDelta        => firstEngineState.lastDelta == otherEngineState.lastDelta
        case EngineStateField.ElapsedCycleTime => firstEngineState.elapsedCycleTime == otherEngineState.elapsedCycleTime

  /** Field of an engine state */
  enum EngineStateField extends Field[EngineStateField, EngineState]:
    case LastUpdate, LastDelta, ElapsedCycleTime
    def values: Seq[EngineStateField] = EngineStateField.values.toSeq

  private final case class EngineStateImpl(
      lastUpdate: Option[Long],
      lastDelta: Long,
      elapsedCycleTime: Long
  ) extends EngineState:
    override def updateElapsedCycleTime(delta: Long): EngineState = copy(elapsedCycleTime = elapsedCycleTime + delta)
    override def update(currentUpdate: Long): EngineState = lastUpdate match
      case None => copy(lastUpdate = Some(currentUpdate))
      case Some(lastUpdate) if currentUpdate >= lastUpdate =>
        val deltaElapsed = currentUpdate - lastUpdate
        copy(lastUpdate = Some(currentUpdate), deltaElapsed, elapsedCycleTime + deltaElapsed)
      case _ => this
