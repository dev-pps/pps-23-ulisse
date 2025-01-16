package architecture

import com.tngtech.archunit.core.importer.{ClassFileImporter, ImportOption}
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.junit.jupiter.api.Test

class Hexagonal:
  private val ENTITIES_PACKAGE          = "entities.."
  private val APPLICATIONS_PACKAGE      = "applications.."
  private val INFRASTRUCTURES_PACKAGE   = "..infrastructures.."
  private val USER_INTERACTIONS_PACKAGE = "..userInteractions.."

  private val importedProjectClasses = new ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .importClasspath()

  private val importedOnlyProjectClasses = new ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .withImportOption(!_.contains("java"))
    .withImportOption(!_.contains("jdk"))
    .withImportOption(!_.contains("test"))
    .withImportOption(!_.contains("maven"))
    .importClasspath()

  @Test
  def checkEntitiesDependencies(): Unit =
    val rule = ArchRuleDefinition.classes().that()
      .resideInAPackage(ENTITIES_PACKAGE)
      .should().onlyBeAccessed().byAnyPackage(ENTITIES_PACKAGE, APPLICATIONS_PACKAGE)
    rule.check(importedProjectClasses)
