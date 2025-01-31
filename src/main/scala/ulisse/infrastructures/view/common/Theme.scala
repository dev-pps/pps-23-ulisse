package ulisse.infrastructures.view.common

import java.awt.Color

object Theme:

  enum Light(val color: Color):
    case background  extends Light(Color.decode("#f7f8ff"))
    case element     extends Light(Color.decode("#d6dbf5"))
    case hover       extends Light(Color.decode("#a2aace"))
    case normalClick extends Light(Color.decode("#418dff"))
    case trueClick   extends Light(Color.decode("#00c6c0"))
    case falseClick  extends Light(Color.decode("#f75959"))

  // ancora da fare
  enum Dark(val color: Color):
    case background  extends Dark(Color.decode("#1e1e1e"))
    case element     extends Dark(Color.decode("#2d2d2d"))
    case hover       extends Dark(Color.decode("#3d3d3d"))
    case normalClick extends Dark(Color.decode("#4d4d4d"))
    case trueClick   extends Dark(Color.decode("#5d5d5d"))
    case falseClick  extends Dark(Color.decode("#6d6d6d"))
