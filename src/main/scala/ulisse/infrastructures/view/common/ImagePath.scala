package ulisse.infrastructures.view.common

/** Path to the images used in the application. */
object ImagePath:
  /** Path to the icons */
  private val iconsPath: String = "icons/"

  /** Path to the menu icons */
  private val menuPath: String = iconsPath + "menu/"

  /** Path to the compact and expand menu icons */
  private val menuCompactExpand: String = menuPath + "compact-expand/"

  /** Icon to add a new element */
  val addSvgIcon: String = iconsPath + "add.svg"

  /** Icon right expand. */
  val rightExpand: String = menuCompactExpand + "right-expand.svg"

  /** Icon right compact. */
  val rightCompact: String = menuCompactExpand + "right-compact.svg"

  /** Icon left expand. */
  val leftExpand: String = menuCompactExpand + "left-expand.svg"

  /** Icon left compact. */
  val leftCompact: String = menuCompactExpand + "left-compact.svg"

  /** Logo for the application */
  val logo: String = "logo.jpg"

  /** Icon for the menu */
  val simulationIcon: String = menuPath + "simulation.svg"

  /** Icon for the settings */
  val settingsIcon: String = menuPath + "settings.svg"

  /** Icon for the train */
  val trainIcon: String = menuPath + "train.svg"

  /** Icon for the map */
  val mapIcon: String = menuPath + "map.svg"

  /** Image of map */
  val station: String = "station.png"

  /** Image of route */
  val routeNormal: String = "route-Normal.png"

  /** Image of route with AV */
  val routeAV: String = "route-AV.png"

  /** Image of route with AC */
  val train: String = "train.png"
