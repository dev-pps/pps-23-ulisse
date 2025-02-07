package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.ComponentConfigurations.{
  ComponentConfiguration,
  ComponentWithConfiguration
}
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.Selectables.Selectable

import scala.swing.{BoxPanel, Component, Label, Orientation, SequentialContainer}

object Cards:
  trait JImageCard extends JItem with SequentialContainer.Wrapper with Selectable:
    val image: ImagePanel
    val content: Component
    def reverse(): JImageCard =
      @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
      val reversedContent = this.contents.reverse
      this.contents.clear()
      this.contents ++= reversedContent
      this

  object JImageCard:
    def horizontal(image: ImagePanel, component: Component, styler: JStyler.JStyler): JImageCard =
      JImageCardImpl(image, ComponentConfiguration.empty(), component, Orientation.Horizontal, styler)

    def vertical(image: ImagePanel, component: Component, styler: JStyler.JStyler): JImageCard =
      JImageCardImpl(image, ComponentConfiguration.empty(), component, Orientation.Vertical, styler)

    def horizontalWithConfiguration(
        imageWithConfiguration: ComponentWithConfiguration[ImagePanel],
        component: Component,
        styler: JStyler.JStyler
    ): JImageCard =
      JImageCardImpl(
        imageWithConfiguration.component,
        imageWithConfiguration.configuration,
        component,
        Orientation.Horizontal,
        styler
      )

    def verticalWithConfiguration(
        imageWithConfiguration: ComponentWithConfiguration[ImagePanel],
        component: Component,
        styler: JStyler.JStyler
    ): JImageCard =
      JImageCardImpl(
        imageWithConfiguration.component,
        imageWithConfiguration.configuration,
        component,
        Orientation.Vertical,
        styler
      )

  private final case class JImageCardImpl(
      image: ImagePanel,
      imageConfiguration: ComponentConfiguration,
      content: Component,
      orientation: Orientation.Value,
      styler: JStyler.JStyler
  ) extends BoxPanel(orientation) with JImageCard with JItem(styler):
    listenTo(image.mouse.clicks, image.mouse.moves, content.mouse.clicks, content.mouse.moves)
    contents += image.align(imageConfiguration.alignment); contents += content
    private val selectedStyler = styler.copy(palette =
      styler.palette.copy(background = styler.palette.clickColor.getOrElse(JStyler.transparentColor))
    )
    override def selected_=(newSelected: Boolean): Unit =
      if newSelected then setStyler(selectedStyler)
      else setStyler(styler)
      super.selected_=(newSelected)

  object Example:
    val imageCardExample: JImageCard =
      Cards.JImageCard.vertical(
        ImagePanels.Example.imagePanelExample,
        Label("Logo").centerHorizontally(),
        JStyler.default
      )
    val svgCardExample: JImageCard =
      Cards.JImageCard.horizontal(
        ImagePanels.Example.svgPanelExample,
        Label("Map").centerHorizontally(),
        JStyler.default
      )
    val drawnCardExample: JImageCard =
      Cards.JImageCard.horizontal(
        ImagePanels.Example.drawnPanelExample,
        Label("Cross").centerHorizontally(),
        JStyler.default
      )
