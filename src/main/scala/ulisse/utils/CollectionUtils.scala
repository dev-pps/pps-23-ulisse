package ulisse.utils

import cats.data.State
import cats.syntax.all.*
import cats.{Functor, Traverse}

/** Defines utility methods for collections. */
object CollectionUtils:
  extension [F[_], A](collection: F[A])(using F: Functor[F])
    /** Update all elements in the collection that satisfy the condition. */
    def updateWhen(condition: A => Boolean)(update: A => A): F[A] =
      F.map(collection)(item => if condition(item) then update(item) else item)

  extension [F[_], A](collection: F[A])(using F: Traverse[F])
    /** Update the first element in the collection that satisfies the condition. */
    def updateFirstWhen(condition: A => Boolean)(update: A => A): F[A] =
      // maybe a fold approach is more readable
      collection.traverse(item =>
        State[Boolean, A] { hasUpdated =>
          if condition(item) && !hasUpdated then (true, update(item))
          else (hasUpdated, item)
        }
      ).runA(false).value
