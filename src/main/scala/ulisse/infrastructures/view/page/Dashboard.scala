package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.{ImagePath, Observers}
import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.event.MouseEvent
import scala.swing.{Component, Orientation}

/** Represents the dashboard of the application. */
trait Dashboard extends ComposedSwing:
  /** Attaches an observer to the new icon. */
  def attachNewIcon(observer: Observers.Observer[MouseEvent]): Unit

object Dashboard:
  private given orientationMenu: Orientation.Value = Orientation.Vertical

  /** Creates a new instance of the dashboard. */
  def apply(): Dashboard = DashboardImpl()

  private case class NewIconEvents(dashboard: Dashboard) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = dashboard.component.visible = false

  private case class DashboardImpl() extends Dashboard:
    private val width = 100

    private val newIcon    = ComposedImageLabel.createToDashboard(ImagePath.addSvgIcon, "new")
    private val iconsPanel = newIcon.centerHorizontally()
    private val mainPanel  = iconsPanel.centerVertically()

    newIcon.withDimension(width, width)
    newIcon.attach(NewIconEvents(this))

    export newIcon.attach as attachNewIcon

    override def component[T >: Component]: T = mainPanel
