package ulisse.utils

import cats.syntax.all.*
import cats.{Id, Monad, Traverse}

/** Defines utility methods for collections. */
object CollectionUtils:
  private def wrappedUpdate[A](update: A => A)(in: A): Id[A] = Id(update(in))

  extension [F[_]: Traverse, A](collection: F[A])
    /** Update all elements in the collection that satisfy the condition. */
    def updateWhen(condition: A => Boolean)(update: A => A): F[A] =
      collection.updateWhenWithEffects(condition)(wrappedUpdate(update))

    /** Swap all elements in the collection that satisfy the condition. */
    def swapWhen(condition: A => Boolean)(element: A): F[A] =
      collection.updateWhen(condition)(_ => element)

    /** Update all elements in the collection that are equal to the given element. */
    def swapWhenEq(find: A)(replace: A): F[A] =
      collection.updateWhen(_ == find)(_ => replace)

    /** Update all elements in the collection that satisfy the condition, handling effects. */
    def updateWhenWithEffects[W[_]: Monad](condition: A => Boolean)(update: A => W[A]): W[F[A]] =
      collection.traverse(item => if condition(item) then update(item) else item.pure[W])

    /** Swap all elements in the collection that satisfy the condition, handling effects. */
    def swapWhenWithEffects[W[_]: Monad](condition: A => Boolean)(element: W[A]): W[F[A]] =
      collection.updateWhenWithEffects(condition)(_ => element)

    /** Swap all elements in the collection that are equal to the given element, handling effects. */
    def swapWhenEqWithEffects[W[_]: Monad](find: A)(replace: W[A]): W[F[A]] =
      collection.updateWhenWithEffects(_ == find)(_ => replace)
