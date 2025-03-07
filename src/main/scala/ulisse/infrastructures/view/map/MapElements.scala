package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.common.Observers.{ClickObserver, Observable}

import java.awt.image.ImageObserver
import scala.swing.Graphics2D
import scala.swing.event.MouseEvent

/** Represent a generic element of the map. */
trait MapElements[T]:

  /** The elements of the map. */
  def find(el: T): Option[T]

  /** Attach the click observer to each element of the map. */
  def attachClick(event: ClickObserver[MapElement[T]]): Unit

  /** Update the elements of the map. */
  def update(newElements: Seq[MapElement[T]]): Unit

  /** Draw the elements on the screen. */
  def draw(g: Graphics2D, observer: ImageObserver): Unit

/** Companion object for [[MapElements]]. */
object MapElements:

  /** Create a new [[MapElements]] with the given [[T]]. */
  def apply[T](map: Observable[MouseEvent]): MapElements[T] = MapElementsImpl[T](map, List.empty)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class MapElementsImpl[T](map: Observable[MouseEvent], var elements: List[MapElement[T]])
      extends MapElements[T]:

    override def find(el: T): Option[T] = elements find (_.element == el) map (_.element)

    override def attachClick(event: ClickObserver[MapElement[T]]): Unit = elements foreach (_ attachClick event)

    override def update(newElements: Seq[MapElement[T]]): Unit =
      detachAllClicks()
      elements foreach (_.image detachFrom map)
      elements = newElements.toList
      elements foreach (_.image attachOn map)

    override def draw(g: Graphics2D, observer: ImageObserver): Unit = elements foreach (_ drawItem (g, observer))

    private def detachAllClicks(): Unit = elements foreach (_.detachAllClicks())
