package ulisse.utils

/** Define boolean operations, to improve readability */
@SuppressWarnings(Array("org.wartremover.warts.Overloading"))
object Booleans:

  /** Define boolean operations, to improve readability */
  extension (b: Boolean)
    /** Logical Not */
    def not: Boolean = !b
