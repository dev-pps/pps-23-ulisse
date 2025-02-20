package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.ComponentConfigurations.{
  ComponentConfiguration,
  ComponentWithConfiguration
}
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.Images.ImagePanel
import ulisse.infrastructures.view.components.Selectables.Selectable
import ulisse.infrastructures.view.components.SwingEnhancements.{EnhancedLook, ShapeEffect}

import scala.swing.*

object Cards:
  trait JImageCard extends EnhancedLook with ShapeEffect with SequentialContainer.Wrapper:
    val image: ImagePanel
    val content: Component
    def reverse(): JImageCard =
      @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
      val reversedContent = this.contents.reverse
      this.contents.clear()
      this.contents ++= reversedContent
      this

  trait SelectableJImageCard extends JImageCard with Selectable

  object JImageCard:
    def horizontal(image: ImagePanel, component: Component): JImageCard =
      JImageCardImpl(image, ComponentConfiguration.empty(), component, Orientation.Horizontal)

    def vertical(image: ImagePanel, component: Component): JImageCard =
      JImageCardImpl(image, ComponentConfiguration.empty(), component, Orientation.Vertical)

    def horizontalWithConfiguration(
        imageWithConfiguration: ComponentWithConfiguration[ImagePanel],
        component: Component
    ): JImageCard =
      JImageCardImpl(
        imageWithConfiguration.component,
        imageWithConfiguration.configuration,
        component,
        Orientation.Horizontal
      )

    def verticalWithConfiguration(
        imageWithConfiguration: ComponentWithConfiguration[ImagePanel],
        component: Component
    ): JImageCard =
      JImageCardImpl(
        imageWithConfiguration.component,
        imageWithConfiguration.configuration,
        component,
        Orientation.Vertical
      )

  object SelectableJImageCard:
    def horizontal(image: ImagePanel, component: Component): SelectableJImageCard =
      SelectableJImageCardImpl(image, ComponentConfiguration.empty(), component, Orientation.Horizontal)

    def vertical(image: ImagePanel, component: Component): SelectableJImageCard =
      SelectableJImageCardImpl(image, ComponentConfiguration.empty(), component, Orientation.Vertical)

    def horizontalWithConfiguration(
        imageWithConfiguration: ComponentWithConfiguration[ImagePanel],
        component: Component
    ): SelectableJImageCard =
      SelectableJImageCardImpl(
        imageWithConfiguration.component,
        imageWithConfiguration.configuration,
        component,
        Orientation.Horizontal
      )

    def verticalWithConfiguration(
        imageWithConfiguration: ComponentWithConfiguration[ImagePanel],
        component: Component
    ): SelectableJImageCard =
      SelectableJImageCardImpl(
        imageWithConfiguration.component,
        imageWithConfiguration.configuration,
        component,
        Orientation.Vertical
      )

  private class BaseJImageCard(
      image: ImagePanel,
      imageConfiguration: ComponentConfiguration,
      content: Component,
      orientation: Orientation.Value
  ) extends BoxPanel(orientation) with EnhancedLook:
    listenTo(image.mouse.clicks, image.mouse.moves, content.mouse.clicks, content.mouse.moves)
    contents += image.align(imageConfiguration.alignment); contents += content

  private final case class JImageCardImpl(
      image: ImagePanel,
      imageConfiguration: ComponentConfiguration,
      content: Component,
      orientation: Orientation.Value
  ) extends BaseJImageCard(image, imageConfiguration, content, orientation)
      with JImageCard

  private final case class SelectableJImageCardImpl(
      image: ImagePanel,
      imageConfiguration: ComponentConfiguration,
      content: Component,
      orientation: Orientation.Value
  ) extends BaseJImageCard(image, imageConfiguration, content, orientation)
      with SelectableJImageCard:
//    private val selectedStyler = styler.copy(palette =
//      styler.palette.copy(background = styler.palette.clickColor.getOrElse(JStyle.transparentColor))
//    )

    override def selected_=(newSelected: Boolean): Unit =
//      if newSelected then setStyler(selectedStyler)
//      else setStyler(styler)
      super.selected_=(newSelected)

  object Example:
    val imageCardExample: JImageCard =
      Cards.JImageCard.vertical(
        Images.Example.imagePanelExample,
        Label("Logo").centerHorizontally()
      )
    val svgCardExample: JImageCard =
      Cards.JImageCard.horizontal(
        Images.Example.svgPanelExample,
        Label("Map").centerHorizontally()
      )
    val drawnCardExample: JImageCard =
      Cards.JImageCard.horizontal(
        Images.Example.drawnPanelExample,
        Label("Cross").centerHorizontally()
      )
