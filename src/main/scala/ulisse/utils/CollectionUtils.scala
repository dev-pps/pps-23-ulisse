package ulisse.utils

import cats.data.State
import cats.syntax.all.*
import cats.{Functor, Traverse}

object CollectionUtils:
  extension [F[_], A](collection: F[A])(using F: Functor[F])
    def updateWhen(condition: A => Boolean)(update: A => A): F[A] =
      F.map(collection)(item => if condition(item) then update(item) else item)
  extension [F[_], A](collection: F[A])(using F: Traverse[F])
    // maybe a fold approach is more readable
    def updateFirstWhen(condition: A => Boolean)(update: A => A): F[A] =
      collection.traverse(item =>
        State[Boolean, A] { hasUpdated =>
          if condition(item) && !hasUpdated then (true, update(item))
          else (hasUpdated, item)
        }
      ).runA(false).value
