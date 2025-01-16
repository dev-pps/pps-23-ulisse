package infrastructures.ui.train

import scala.swing.{Component, FlowPanel, TextField}
import scala.swing.event.ValueChanged

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
        if (!text.matches("^[0-9]*$")) {
          text = text.filter(_.isDigit)
        }
    }
