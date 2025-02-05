package ulisse.architecture

import com.tngtech.archunit.core.importer.{ClassFileImporter, Location}

object Packages:
  val PROJECT = "..ulisse.."

  val ENTITIES        = "..entities.."
  val APPLICATIONS    = "..applications.."
  val INFRASTRUCTURES = "..infrastructures.."
  val ADAPTERS        = "..adapters.."
  val UTILS           = "..utils.."

  val PORTS     = "..ports.."
  val MANAGERS  = "..managers.."
  val USE_CASES = "..useCases.."

object ArchUnits:
  private val DO_NOT_INCLUDE_SCALA_COMPILED_FILE: Location => Boolean = !_.contains("$")
  private val DO_NOT_INCLUDE_CLASSES_TEST: Location => Boolean        = !_.contains("test-classes")
  private val PRINT: Location => Boolean = location =>
    println(location)
    true

  val IMPORT_ONLY_CLASSES_CREATED = new ClassFileImporter()
    .withImportOption(DO_NOT_INCLUDE_SCALA_COMPILED_FILE(_))
    .withImportOption(DO_NOT_INCLUDE_CLASSES_TEST(_))
    .importPackages(Packages.PROJECT)
