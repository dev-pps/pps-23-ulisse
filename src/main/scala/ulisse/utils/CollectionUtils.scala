package ulisse.utils

import cats.data.{State, StateT}
import cats.syntax.all.*
import cats.{Functor, Id, Monad, Traverse}

/** Defines utility methods for collections. */
object CollectionUtils:

  private def wrappedUpdate[A](update: A => A)(in: A): Id[A] = Id(update(in))

  extension [F[_]: Traverse, A](collection: F[A])
    /** Update all elements in the collection that satisfy the condition, handling effects. */
    def updateWhen(condition: A => Boolean)(update: A => A): F[A] =
      collection.updateWhenWithEffects(condition)(wrappedUpdate(update))
    def updateWhenWithEffects[W[_]: Monad](condition: A => Boolean)(update: A => W[A]): W[F[A]] =
      collection.traverse(item => if condition(item) then update(item) else item.pure[W])

  extension [F[_]: Traverse, A](collection: F[A])
    /** Update the first element in the collection that satisfies the condition. */
    def updateFirstWhen(condition: A => Boolean)(update: A => A): F[A] =
      collection.updateFirstWhenWithEffects(condition)(wrappedUpdate(update))
    def updateFirstWhenWithEffects[W[_]: Monad](condition: A => Boolean)(update: A => W[A]): W[F[A]] =
      collection.traverse { item =>
        StateT[W, Boolean, A] { hasUpdated =>
          if condition(item) && !hasUpdated then
            update(item).map(updated => (true, updated))
          else
            (hasUpdated, item).pure[W]
        }
      }.runA(false)
