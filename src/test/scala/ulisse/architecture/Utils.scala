package ulisse.architecture

import com.tngtech.archunit.core.importer.{ClassFileImporter, Location}

object Packages:
  val PROJECT           = "..ulisse.."
  val ENTITIES          = "..entities.."
  val APPLICATIONS      = "..applications.."
  val INFRASTRUCTURES   = "..infrastructures.."
  val USER_INTERACTIONS = "..userInteractions.."

  val USE_CASES = "..useCases.."

object ArchUnits:
  private val DO_NOT_INCLUDE_SCALA_COMPILED_FILE: Location => Boolean = !_.contains("$")

  val IMPORT_ONLY_CLASSES_CREATED = new ClassFileImporter()
    .withImportOption(DO_NOT_INCLUDE_SCALA_COMPILED_FILE(_))
    .importPackages(Packages.PROJECT)
