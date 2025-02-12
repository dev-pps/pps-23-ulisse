package ulisse.utils

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.utils.Errors.{BaseError, ErrorMessage}

/** Utility object containing various validation functions.
  *
  * This object provides common validation utilities to check conditions on values
  *
  * @note
  *   These methods return an `Either[E, T]` where `E` is an error type and `T` is the valid value. If validation
  *   passes, the `Right` side of the `Either` is returned, otherwise the `Left` side contains the provided error.
  */
/** Utility methods for validating various types of input values.
  *
  * Provides functions to validate numeric ranges, positivity, non-negativity, non-blank strings, and uniqueness of
  * collection items. Each function returns either a validated value or an error.
  */
object ValidationUtils:

  /** Validates that the value is within the specified range (inclusive).
    *
    * @tparam N
    *   The type of the value, which must be a numeric type (e.g., `Int`, `Double`).
    * @tparam E
    *   The type of the error, which must extend `BaseError`.
    * @param value
    *   The value to validate.
    * @param min
    *   The minimum valid value (inclusive).
    * @param max
    *   The maximum valid value (inclusive).
    * @param error
    *   The error to return if the value is not within the range.
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
    * @tparam N
    *   The type of the value, which must be a numeric type (e.g., `Int`, `Double`).
    * @tparam E
    *   The type of the error, which must extend `BaseError`.
    * @param value
    *   The value to validate.
    * @param error
    *   The error to return if the value is negative.
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
    * @tparam N
    *   The type of the value, which must be a numeric type (e.g., `Int`, `Double`).
    * @tparam E
    *   The type of the error, which must extend `BaseError`.
    * @param value
    *   The value to validate.
    * @param error
    *   The error to return if the value is not positive.
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
    * @tparam E
    *   The type of the error, which must extend `BaseError`.
    * @param value
    *   The string to validate.
    * @param error
    *   The error to return if the string is blank.
    * @return
    *   Returns `Right(value)` if the string is not blank, otherwise `Left(error)`.
    */
  def validateNonBlankString[E <: BaseError](value: String, error: E): Either[E, String] =
    Either.cond(!value.isBlank, value, error)

  /** Validates that all items in a collection are unique.
    *
    * @tparam A
    *   The type of the items in the collection.
    * @tparam E
    *   The type of the error, which must extend `BaseError`.
    * @param items
    *   The collection of items to validate.
    * @param error
    *   The error to return if the items are not unique.
    * @return
    *   Returns `Right(items)` if the items are unique, otherwise `Left(error)`.
    */
  def validateUniqueItems[A, E <: BaseError](items: Seq[A], error: E): Either[E, Seq[A]] =
    Either.cond(items.distinct.size === items.size, items, error)

  extension [A, E](value: A)
    def cond(f: A => Boolean, error: E): Either[E, A] = Either.cond(f(value), value, error)

    def validateChain(f: (A => Boolean, E)*): Either[NonEmptyChain[E], A] =
      f.map(value.cond).traverse(_.toValidatedNec).map(_ => value).toEither

  extension [A <: ErrorMessage](chainErrors: NonEmptyChain[A])
    def mkStringErrors: String = chainErrors.toList.map(_.msg).mkString(", ")
