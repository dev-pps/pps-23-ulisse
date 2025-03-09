package ulisse.infrastructures.view.utils

import ulisse.infrastructures.view.components.ExtendedSwing.SFlowPanel
import ulisse.infrastructures.view.components.styles.Styles

import scala.swing.*

/** Utility methods for Swing components. */
object SwingUtils:

  /** Default font for Swing components */
  val nameFont = new Font("Arial", java.awt.Font.BOLD, 18)

  /** Default font for Swing components */
  val labelFont = new Font("Arial", java.awt.Font.BOLD, 14)

  /** Default font for Swing components */
  val valueFont = new Font("Arial", java.awt.Font.PLAIN, 14)

  /** Returns default string "N/A" if string `s` is not present */
  extension (s: Option[String])
    def defaultString: String = s.getOrElse("N/A")

  /** Returns default string "N/A" if given int `i` is None */
  extension (i: Option[Int])
    def defaultIntString: String = i.map(_.toString).defaultString

  extension (text: String)
    /** Returns [[Label]] with bold font and size 18pt */
    def headerLabel: Label =
      new Label(text) {
        font = labelFont
      }

    /** Returns [[Label]] with plain font and size 14pt */
    def valueLabel: Label =
      new Label(text) {
        font = valueFont
      }

  /** Open component `c` inside a [[MainFrame]]. */
  extension (c: Component)
    def showPreview(): MainFrame =
      new MainFrame() {
        title = "timetable preview"
        val mainPanel: SFlowPanel = SFlowPanel()
        mainPanel.contents += c
        contents = mainPanel
        visible = true
      }

  /** Set font style of each component in `components` to [[Styles.defaultFont.swingFont]] */
  extension (components: Seq[Component])
    def setDefaultFont(): Unit =
      components.foreach(_.font = Styles.defaultFont.swingFont)

  /** Updates `combobox` model to new one `model` */
  extension [A](combobox: ComboBox[A])
    def updateModel(model: Seq[A]): Unit =
      Swing.onEDT:
        combobox.peer.setModel(ComboBox.newConstantModel(model))

  /** Returns optionally the `combobox` selected item */
  extension [T](comboBox: ComboBox[T])
    def selectedItemOption: Option[T] = Option.when(comboBox.selection.item != null)(comboBox.selection.item)

  /** Returns `items` vertically spaced with `space` between */
  extension (items: Seq[Component])
    def vSpaced(space: Int): Seq[Component] =
      import scala.swing.Swing.VStrut
      items.flatMap(field => List(field, VStrut(space)))
