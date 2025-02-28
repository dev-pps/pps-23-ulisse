package ulisse.infrastructures.view.train

import ulisse.infrastructures.view.components.ExtendedSwing.{SButton, STextField}
import ulisse.infrastructures.view.components.styles.Styles

import scala.swing.event.ValueChanged
import scala.swing.{ComboBox, Component, FlowPanel, Font, Graphics2D, Label, Swing, TextField}

object SwingUtils:

  val nameFont  = new Font("Arial", java.awt.Font.BOLD, 18)
  val labelFont = new Font("Arial", java.awt.Font.BOLD, 14)
  val valueFont = new Font("Arial", java.awt.Font.PLAIN, 14)

  extension (s: Option[String])
    implicit def defaultString: String =
      s.getOrElse("N/A")

  extension (i: Option[Int])
    implicit def defaultIntString: String =
      i match
        case Some(v) => v.toString
        case None    => "N/A"

  extension (text: String)
    def headerLabel: Label =
      new Label(text) {
        font = labelFont
      }

    def valueLabel: Label =
      new Label(text) {
        font = valueFont
      }

  class SNumberField(cols: Int) extends STextField(cols):
    reactions += {
      case ValueChanged(_) => Swing.onEDT(if (!text.matches("^[0-9]*$")) text = text.filter(_.isDigit))
    }

  class StyledButton(label: String) extends SButton(label):
    this.rect = Styles.defaultRect.withPaddingWidthAndHeight(20, 10)
