package ulisse.utils

import ulisse.utils.Errors.AppError

object ValidationUtils:
  def validateRange[N: Numeric](
      value: N,
      min: N,
      max: N,
      error: AppError
  )(using numeric: Numeric[N]): Either[AppError, N] =
    Either.cond(numeric.gteq(value, min) && numeric.lteq(value, max), value, error)

  def validateNonNegative[N: Numeric](
      value: N,
      error: AppError
  )(using numeric: Numeric[N]): Either[AppError, N] =
    Either.cond(numeric.gteq(value, numeric.zero), value, error)
