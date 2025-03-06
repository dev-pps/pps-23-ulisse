package ulisse.adapters

import ulisse.adapters.input.{RouteAdapter, StationEditorAdapter}
import ulisse.applications.InputPortManager

/** Represents the input adapter manager of the application. */
trait InputAdapterManager:
  /** The station editor adapter. */
  val station: StationEditorAdapter

  /** The route adapter. */
  val route: RouteAdapter

/** Companion object of the input adapter manager. */
object InputAdapterManager:

  /** Creates a new instance of the input adapter manager. */
  def apply(ports: InputPortManager): InputAdapterManager = new InputAdapterManagerImpl(ports)

  private final case class InputAdapterManagerImpl(station: StationEditorAdapter, route: RouteAdapter)
      extends InputAdapterManager:
    def this(ports: InputPortManager) =
      this(StationEditorAdapter(ports.station), RouteAdapter(ports.route))
