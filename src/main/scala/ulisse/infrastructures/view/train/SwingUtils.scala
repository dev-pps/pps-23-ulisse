package ulisse.infrastructures.view.train

import java.awt.{BasicStroke, Color, RenderingHints}
import javax.swing.border.AbstractBorder
import scala.swing.event.ValueChanged
import scala.swing.{Component, FlowPanel, Font, Graphics2D, Label, Swing, TextField}

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

  extension (c: Component)
    def onLeftOf(c1: Component): Component =
      new FlowPanel() {
        contents += c
        contents += c1
      }

  class NumberField(cols: Int) extends TextField:
    columns = cols
    listenTo(this)
    reactions += {
      case ValueChanged(_) =>
        Swing.onEDT {
          if (!text.matches("^[0-9]*$")) {
            text = text.filter(_.isDigit)
          }
        }
    }
