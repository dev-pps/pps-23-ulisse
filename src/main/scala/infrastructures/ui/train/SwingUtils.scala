package infrastructures.ui.train

import scala.swing.TextField
import scala.swing.event.ValueChanged

object SwingUtils:
  class NumberField(cols: Int) extends TextField:
    columns = cols
    listenTo(this)
    reactions += {
      case ValueChanged(_) =>
        if (!text.matches("^[0-9]*$")) {
          text = text.filter(_.isDigit)
        }
    }
