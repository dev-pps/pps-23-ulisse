package ulisse.utils

/** Defines the basic error message types for the application. */
object Errors:

  /** Represents the base error. */
  trait BaseError

  /** Represents the error message. */
  trait ErrorMessage(val msg: String) extends BaseError:
    override def toString: String = msg

  /** Represents the error not found. */
  class ErrorNotFound(private val text: String) extends ErrorMessage(s"[NOT-FOUND] $text")

  /** Represents the error validation. */
  class ErrorValidation(private val text: String) extends ErrorMessage(s"[NOT-VALID] $text")

  /** Represents the error not exist. */
  class ErrorNotExist(private val text: String) extends ErrorMessage(s"[NOT-EXIST] $text")
