package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.{ImagePath, Observers}
import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.event.MouseEvent
import scala.swing.{Component, Orientation}

/** Represents the menu of the application. */
trait Menu extends ComposedSwing:
  /** Attaches an observer to the new icon. */
  def attachNewIcon(observer: Observers.Observer[MouseEvent]): Unit

object Menu:
  private given orientationMenu: Orientation.Value = Orientation.Vertical

  /** Creates a new instance of the menu. */
  def apply(): Menu = MenuImpl()

  private case class NewIconEvents(menu: Menu) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = menu.component.visible = false

  private case class MenuImpl() extends Menu:
    private val width = 100

    private val newIcon    = ComposedImageLabel.createToMenu(ImagePath.addSvgIcon, "new")
    private val iconsPanel = newIcon.centerHorizontally()
    private val mainPanel  = iconsPanel.centerVertically()

    newIcon.withDimension(width, width)
    newIcon.attach(NewIconEvents(this))

    export newIcon.attach as attachNewIcon

    override def component[T >: Component]: T = mainPanel
