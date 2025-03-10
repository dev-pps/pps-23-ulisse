package ulisse.infrastructures.utils

/** Provides time-related utilities. */
object TimeProviders:
  /** A provider of the current time. */
  trait TimeProvider:
    /** Returns the current time in milliseconds. */
    def currentTimeMillis(): Long

  /** Factory for [[TimeProvider]] instances. */
  object TimeProvider:
    /** Creates a system TimeProvider. */
    def systemTimeProvider(): TimeProvider = SystemTimeProvider()

    private final case class SystemTimeProvider() extends TimeProvider:
      override def currentTimeMillis(): Long = System.currentTimeMillis()
