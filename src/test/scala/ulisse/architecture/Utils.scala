package ulisse.architecture

import com.tngtech.archunit.core.importer.{ClassFileImporter, ImportOption, Location}

object Packages:
  private val applications    = "applications"
  private val infrastructures = "infrastructures"
  private val adapters        = "adapters"

  val PROJECT           = "..ulisse.."
  val ENTITIES          = "..entities.."
  val APPLICATIONS      = ".." + applications + ".."
  val INFRASTRUCTURES   = ".." + infrastructures + ".."
  val USER_INTERACTIONS = "..userInteractions.."

  val PORTS          = "..ports.."
  val USE_CASES      = "..useCases.."
  val INPUT_ADAPTER  = ".." + applications + "." + adapters + ".."
  val OUTPUT_ADAPTER = ".." + infrastructures + "." + adapters + ".."

object ArchUnits:
  private val DO_NOT_INCLUDE_SCALA_COMPILED_FILE: Location => Boolean = !_.contains("$")
  private val DO_NOT_INCLUDE_TEST_CLASSES: Location => Boolean        = !_.contains("test-classes")
  private val PRINT_IMPORTS: Location => Boolean = location =>
    println(location); true

  val IMPORT_ONLY_CLASSES_CREATED = new ClassFileImporter()
    .withImportOption(DO_NOT_INCLUDE_SCALA_COMPILED_FILE(_))
    .withImportOption(DO_NOT_INCLUDE_TEST_CLASSES(_))
    .importPackages(Packages.PROJECT)
