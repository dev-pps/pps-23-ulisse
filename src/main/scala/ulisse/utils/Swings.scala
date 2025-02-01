package ulisse.utils

import scala.swing.{Container, Point}

object Swings:

  extension (a: Container)
    def centerOf(b: Container): Unit =
      val dialogWidth: Int  = a.size.width
      val dialogHeight: Int = a.size.height
      val x: Int            = b.location.x + (b.size.width - dialogWidth) / 2
      val y: Int            = b.location.y + (b.size.height - dialogHeight) / 2
      a.peer.setLocation(x, y)
