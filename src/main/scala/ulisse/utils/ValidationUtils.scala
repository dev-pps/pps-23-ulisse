package ulisse.utils

import cats.implicits.catsSyntaxEq
import ulisse.utils.Errors.BaseError

object ValidationUtils:
  def validateRange[N: Numeric, E <: BaseError](
      value: N,
      min: N,
      max: N,
      error: E
  )(using numeric: Numeric[N]): Either[E, N] =
    Either.cond(numeric.gteq(value, min) && numeric.lteq(value, max), value, error)

  def validateNonNegative[N: Numeric, E <: BaseError](
      value: N,
      error: E
  )(using numeric: Numeric[N]): Either[E, N] =
    Either.cond(numeric.gteq(value, numeric.zero), value, error)

  def validatePositive[N: Numeric, E <: BaseError](
      value: N,
      error: E
  )(using numeric: Numeric[N]): Either[E, N] =
    Either.cond(numeric.gt(value, numeric.zero), value, error)

  def validateNonBlankString[E <: BaseError](value: String, error: E): Either[E, String] =
    Either.cond(!value.isBlank, value, error)

  def validateUniqueItems[E <: BaseError](items: Seq[?], error: E): Either[E, Seq[?]] =
    Either.cond(items.distinct.size === items.size, items, error)
