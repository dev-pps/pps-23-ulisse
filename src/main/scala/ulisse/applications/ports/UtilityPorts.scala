package ulisse.applications.ports

/** Ports for utility services */
object UtilityPorts:
  /** Port for output services */
  object Output:
    /** Port for TimeProvider service */
    trait TimeProviderPort:
      /** Get the current time in milliseconds */
      def currentTimeMillis(): Long
