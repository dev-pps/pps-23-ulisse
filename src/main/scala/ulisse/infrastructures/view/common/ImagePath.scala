package ulisse.infrastructures.view.common

object ImagePath:
  private val iconsPath: String = "icons/"
  private val menuPath: String  = iconsPath + "menu/"

  /** Icon to add a new element */
  val addSvgIcon: String = iconsPath + "add.svg"

  /** Icons to control panel */
  val expand: String  = iconsPath + "expand.svg"
  val compact: String = iconsPath + "compact.svg"

  /** Logo for the application */
  val logo: String = "logo.jpg"

  /** Icon for the menu */
  val simulation: String = menuPath + "simulation.svg"
  val settings: String   = menuPath + "settings.svg"
  val train: String      = menuPath + "train.svg"
  val map: String        = menuPath + "map.svg"
