package ulisse.infrastructures.view.common

import scala.reflect.ClassTag
import scala.swing.{Component, Dimension, Panel}

trait KeyValuesPanel[+P <: Panel] extends WrapPanel[P]:
  def values[T <: Component](implicit ct: reflect.ClassTag[T]): Seq[T];

object KeyValuesPanel:
  def apply[P <: Panel](panel: P)(key: Component, values: Component*)(using opaque: Boolean): KeyValuesPanel[P] =
    KeyValuesPanelImpl(panel)(key, values: _*)

  private case class KeyValuesPanelImpl[+P <: Panel](mainPanel: P)(key: Component, values: Component*)(using
      opaque: Boolean
  ) extends KeyValuesPanel[P]:
    private val wrapPanel: WrapPanel[P] = WrapPanel(mainPanel)(Seq(key).appendedAll(values): _*)

    mainPanel.peer.add(key.peer)
    values.foreach(component => mainPanel.peer.add(component.peer))

    export wrapPanel.*
    override def values[T <: Component](implicit ct: ClassTag[T]): Seq[T] = wrapPanel.componentsOf[T]

trait PairPanel[+P <: Panel, +A <: Component, +B <: Component] extends WrapPanel[P]:
  def key: A
  def value: B

object PairPanel:
  def apply[P <: Panel, A <: Component, B <: Component](panel: P, first: A, second: B)(using
      opaque: Boolean
  ): PairPanel[P, A, B] = PairPanelImpl(panel, first, second)

  private case class PairPanelImpl[+P <: Panel, +A <: Component, +B <: Component](mainPanel: P, first: A, second: B)(
      using opaque: Boolean
  ) extends PairPanel[P, A, B]:
    private val wrapPanel: WrapPanel[P] = WrapPanel(mainPanel)(first, second)
    mainPanel.peer.add(first.peer)
    mainPanel.peer.add(second.peer)
    mainPanel.maximumSize = new Dimension(400, 40)

    export wrapPanel.*
    override def key: A   = first
    override def value: B = second
