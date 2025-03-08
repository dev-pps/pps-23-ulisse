package ulisse.utils

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.utils.Errors.{BaseError, ErrorMessage}

/** Utility methods for validating input values. */
object ValidationUtils:
  /** Validates that the value is within the specified range (inclusive). */
  def validateRange[N: Numeric, E <: BaseError](
      value: N,
      min: N,
      max: N,
      error: E
  )(using numeric: Numeric[N]): Either[E, N] =
    Either.cond(numeric.gteq(value, min) && numeric.lteq(value, max), value, error)

  /** Validates that the value is non-negative (greater than or equal to zero). */
  def validateNonNegative[N: Numeric, E <: BaseError](
      value: N,
      error: E
  )(using numeric: Numeric[N]): Either[E, N] =
    Either.cond(numeric.gteq(value, numeric.zero), value, error)

  /** Validates that the value is positive (greater than zero). */
  def validatePositive[N: Numeric, E <: BaseError](
      value: N,
      error: E
  )(using numeric: Numeric[N]): Either[E, N] =
    Either.cond(numeric.gt(value, numeric.zero), value, error)

  /** Validates that the string is not blank (it is not empty or only whitespace). */
  def validateNonBlankString[E <: BaseError](value: String, error: E): Either[E, String] =
    Either.cond(!value.isBlank, value, error)

  /** Validates that all items in a collection are unique. */
  def validateUniqueItems[A, E <: BaseError](items: Seq[A], error: E): Either[E, Seq[A]] =
    Either.cond(items.distinct.size === items.size, items, error)

  /** Validates that all items in a collection are unique after applying a transformation function. */
  def validateUniqueItemsBy[A, B, E <: BaseError](items: Seq[A], transform: A => B, error: E): Either[E, Seq[A]] =
    Either.cond(items.distinctBy(transform).size === items.size, items, error)

  extension [A, E](value: A)
    def cond(f: A => Boolean, error: E): Either[E, A] = Either.cond(f(value), value, error)

    def validateChain(f: (A => Boolean, E)*): Either[NonEmptyChain[E], A] =
      f.map(value.cond).traverse(_.toValidatedNec).map(_ => value).toEither

  extension [A <: ErrorMessage](chainErrors: NonEmptyChain[A])
    def mkMsgErrors: String = chainErrors.toList.map(_.msg).mkString(", ")

  extension [A <: BaseError](chainErrors: NonEmptyChain[A])
    def mkErrors: String = chainErrors.toList.map(e => s"$e").mkString(", ")
