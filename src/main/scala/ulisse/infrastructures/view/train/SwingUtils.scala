package ulisse.infrastructures.view.train

import scala.swing.event.ValueChanged
import scala.swing.{Component, FlowPanel, Swing, TextField}

object SwingUtils:
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
