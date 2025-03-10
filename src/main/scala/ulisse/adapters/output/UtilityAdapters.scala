package ulisse.adapters.output

import ulisse.applications.ports.UtilityPorts.Output.TimeProviderPort
import ulisse.infrastructures.utils.TimeProviders.TimeProvider

/** Adapter for the UtilityPorts. */
object UtilityAdapters:
  /** Adapter for the TimeProviderPort. */
  case class TimeProviderAdapter(private val timeProvider: TimeProvider) extends TimeProviderPort:
    override def currentTimeMillis(): Long = timeProvider.currentTimeMillis()
