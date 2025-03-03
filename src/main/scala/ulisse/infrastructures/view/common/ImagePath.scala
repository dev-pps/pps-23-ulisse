package ulisse.infrastructures.view.common

object ImagePath:
  private val iconsPath: String         = "icons/"
  private val menuPath: String          = iconsPath + "menu/"
  private val menuCompactExpand: String = menuPath + "compact-expand/"

  /** Icon to add a new element */
  val addSvgIcon: String = iconsPath + "add.svg"

  /** Icons to control panel */
  val rightExpand: String  = menuCompactExpand + "right-expand.svg"
  val rightCompact: String = menuCompactExpand + "right-compact.svg"
  val leftExpand: String   = menuCompactExpand + "left-expand.svg"
  val leftCompact: String  = menuCompactExpand + "left-compact.svg"

  /** Logo for the application */
  val logo: String = "logo.jpg"

  /** Icon for the menu */
  val simulation: String = menuPath + "simulation.svg"
  val settings: String   = menuPath + "settings.svg"
  val train: String      = menuPath + "train.svg"
  val map: String        = menuPath + "map.svg"

  /** Image of map */
  val station: String = "station.png"
