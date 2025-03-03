package ulisse.adapters

import ulisse.adapters.input.StationEditorAdapter
import ulisse.applications.InputPortManager
import ulisse.applications.ports.{RoutePorts, StationPorts, TimetablePorts, TrainPorts}

/** Represents the input adapter manager of the application. */
trait InputAdapterManager

object InputAdapterManager:

  /** Creates a new instance of the input adapter manager. */
  def apply(ports: InputPortManager): InputAdapterManager = new InputAdapterManagerImpl(ports)

  private final case class InputAdapterManagerImpl(stationAdapter: StationEditorAdapter) extends InputAdapterManager:
    def this(ports: InputPortManager) = this(StationEditorAdapter(ports.station))
