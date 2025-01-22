package ulisse.utils

object Errors:

  trait BaseError
  trait ErrorMessage(val msg: String) extends BaseError

  class ErrorNotFound(private val text: String)   extends ErrorMessage(s"[NOT-FOUND] $text")
  class ErrorValidation(private val text: String) extends ErrorMessage(s"[NOT-VALID] $text")
  class ErrorNotExist(private val text: String)   extends ErrorMessage(s"[NOT-EXIST] $text")
