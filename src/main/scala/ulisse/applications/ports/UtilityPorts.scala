package ulisse.applications.ports

object UtilityPorts:
  object Output:
    trait TimeProviderPort:
      def currentTimeMillis: Long
