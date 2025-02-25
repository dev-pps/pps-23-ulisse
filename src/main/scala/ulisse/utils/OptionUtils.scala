package ulisse.utils

/** Defines utility methods for `Option` objects. */
object OptionUtils:
  extension [A](optionalResult: => A)
    /** Compute and returns an `Option` containing the result if the condition is `true`, otherwise `None`. */
    def when(condition: Boolean): Option[A] =
      Option.when(condition)(optionalResult)

  given [A]: Conversion[Option[Option[A]], Option[A]] = _.flatten
