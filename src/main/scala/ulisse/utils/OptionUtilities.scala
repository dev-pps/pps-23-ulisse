package ulisse.utils

object OptionUtilities:
  extension [A](optionalResult: => A)
    def when(condition: Boolean): Option[A] =
      if condition then Some(optionalResult) else None
