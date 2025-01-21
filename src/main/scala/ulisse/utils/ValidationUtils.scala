package ulisse.utils

import cats.implicits.catsSyntaxEq
import ulisse.utils.Errors.BaseError

/** Utility object containing various validation functions.
  *
  * This object provides common validation utilities to check conditions on values
  *
  * @note
  *   These methods return an `Either[E, T]` where `E` is an error type and `T` is the valid value. If validation
  *   passes, the `Right` side of the `Either` is returned, otherwise the `Left` side contains the provided error.
  */
object ValidationUtils:

  /** Validates that the value is within the specified range (inclusive).
    *
    * @param value
    *   The value to validate.
    * @param min
    *   The minimum valid value (inclusive).
    * @param max
    *   The maximum valid value (inclusive).
    * @param error
    *   The error to return if the value is not within the range.
    * @tparam N
    *   The type of the value, which must be a numeric type (e.g., `Int`, `Double`).
    * @tparam E
    *   The type of the error, which must extend `BaseError`.
    * @return
    *   Returns `Right(value)` if the value is within the range, otherwise `Left(error)`.
    */
  def validateRange[N: Numeric, E <: BaseError](
      value: N,
      min: N,
      max: N,
      error: E
  )(using numeric: Numeric[N]): Either[E, N] =
    Either.cond(numeric.gteq(value, min) && numeric.lteq(value, max), value, error)

  /** Validates that the value is non-negative (i.e., greater than or equal to zero).
    *
    * @param value
    *   The value to validate.
    * @param error
    *   The error to return if the value is negative.
    * @tparam N
    *   The type of the value, which must be a numeric type (e.g., `Int`, `Double`).
    * @tparam E
    *   The type of the error, which must extend `BaseError`.
    * @return
    *   Returns `Right(value)` if the value is non-negative, otherwise `Left(error)`.
    */
  def validateNonNegative[N: Numeric, E <: BaseError](
      value: N,
      error: E
  )(using numeric: Numeric[N]): Either[E, N] =
    Either.cond(numeric.gteq(value, numeric.zero), value, error)

  /** Validates that the value is positive (i.e., greater than zero).
    *
    * @param value
    *   The value to validate.
    * @param error
    *   The error to return if the value is not positive.
    * @tparam N
    *   The type of the value, which must be a numeric type (e.g., `Int`, `Double`).
    * @tparam E
    *   The type of the error, which must extend `BaseError`.
    * @return
    *   Returns `Right(value)` if the value is positive, otherwise `Left(error)`.
    */
  def validatePositive[N: Numeric, E <: BaseError](
      value: N,
      error: E
  )(using numeric: Numeric[N]): Either[E, N] =
    Either.cond(numeric.gt(value, numeric.zero), value, error)

  /** Validates that the string is not blank (i.e., it is not empty or only whitespace).
    *
    * @param value
    *   The string to validate.
    * @param error
    *   The error to return if the string is blank.
    * @tparam E
    *   The type of the error, which must extend `BaseError`.
    * @return
    *   Returns `Right(value)` if the string is not blank, otherwise `Left(error)`.
    */
  def validateNonBlankString[E <: BaseError](value: String, error: E): Either[E, String] =
    Either.cond(!value.isBlank, value, error)

  /** Validates that all items in a collection are unique.
    *
    * @param items
    *   The collection of items to validate.
    * @param error
    *   The error to return if the items are not unique.
    * @tparam E
    *   The type of the error, which must extend `BaseError`.
    * @return
    *   Returns `Right(items)` if the items are unique, otherwise `Left(error)`.
    */
  def validateUniqueItems[A, E <: BaseError](items: Seq[A], error: E): Either[E, Seq[A]] =
    Either.cond(items.distinct.size === items.size, items, error)
