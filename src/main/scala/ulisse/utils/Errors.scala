package ulisse.utils

/** Defines the basic error message types for the application. */
object Errors:

  trait BaseError

  trait ErrorMessage(val msg: String) extends BaseError

  class ErrorNotFound(private val text: String)   extends ErrorMessage(s"[NOT-FOUND] $text")
  class ErrorValidation(private val text: String) extends ErrorMessage(s"[NOT-VALIDATE] $text")
  class ErrorExist(private val text: String)      extends ErrorMessage(s"[NOT-EXIST] $text")
