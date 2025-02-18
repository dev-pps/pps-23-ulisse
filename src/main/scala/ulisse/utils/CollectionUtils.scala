package ulisse.utils

import cats.Functor

object CollectionUtils:
  extension [F[_], A](collection: F[A])(using F: Functor[F])
    def updateWhen(condition: A => Boolean)(update: A => A): F[A] =
      F.map(collection)(item => if condition(item) then update(item) else item)
