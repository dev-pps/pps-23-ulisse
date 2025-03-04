package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.common.Observers.Observable

import java.awt.image.ImageObserver
import scala.swing.Graphics2D
import scala.swing.event.MouseEvent

/** Represent a generic element of the map. */
trait MapElements[T]:

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

    override def update(newElements: Seq[MapElement[T]]): Unit =
      elements.foreach(_.image detachFrom map)
      elements = newElements.toList
      elements.foreach(_.image attachOn map)

    override def draw(g: Graphics2D, observer: ImageObserver): Unit = elements.foreach(_ drawItem (g, observer))
