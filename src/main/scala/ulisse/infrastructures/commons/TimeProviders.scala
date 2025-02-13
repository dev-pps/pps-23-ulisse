package ulisse.infrastructures.commons

object TimeProviders:
  trait TimeProvider:
    def currentTimeMillis(): Long

  object TimeProvider:
    def systemTimeProvider(): TimeProvider = SystemTimeProvider()

    private final case class SystemTimeProvider() extends TimeProvider:
      override def currentTimeMillis(): Long = System.currentTimeMillis()
