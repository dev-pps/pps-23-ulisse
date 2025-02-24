package ulisse.adapters.output

import ulisse.applications.ports.UtilityPorts.Output.TimeProviderPort
import ulisse.infrastructures.commons.TimeProviders.TimeProvider

object UtilityAdapters:
  case class TimeProviderAdapter(private val timeProvider: TimeProvider) extends TimeProviderPort:
    override def currentTimeMillis(): Long = timeProvider.currentTimeMillis()
