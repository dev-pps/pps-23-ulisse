package architecture

import com.tngtech.archunit.core.importer.{ClassFileImporter, ImportOption}
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.junit.jupiter.api.Test

class Hexagonal:
  private val ENTITIES_PACKAGE          = "..entities.."
  private val APPLICATIONS_PACKAGE      = "..applications.."
  private val INFRASTRUCTURES_PACKAGE   = "..infrastructures.."
  private val USER_INTERACTIONS_PACKAGE = "..userInteractions.."

  private val importedProjectClasses = new ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .importClasspath()

  private val importOnlyClassesCreated = new ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TEST_FIXTURES)
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_PACKAGE_INFOS)
    .withImportOption(!_.contains("java"))
    .withImportOption(!_.contains("jdk"))
    .withImportOption(!_.contains("test"))
    .withImportOption(!_.contains("maven"))
    // Selectable è un'interfaccia introdotta dal compilatore Scala 3 (Dotty) per
    // abilitare alcune funzionalità avanzate come il Reflective Access tramite tipi
    // strutturati. Viene aggiunta automaticamente alle classi che utilizzano tipi
    // strutturati o altre caratteristiche che richiedono questo supporto.
    .withImportOption(!_.contains("Selectable"))
    // per togliere la gestione interna del compilatore di scala
    // ovvero: companion object
    .withImportOption(!_.contains("$"))
    .withImportOption(location =>
      println(location)
      true
    )
    .importClasspath()

  @Test
  def checkEntitiesDependencies(): Unit =
//    ArchConfiguration.get.addPackagesToIgnore("entities\\.Coordinate.*", "scala\\..*")

    ArchRuleDefinition.classes().that().haveNameMatching(".*\\$.*") // Per ignorare classi inner di Scala
      .should.resideInAnyPackage()

    val str = "NUMERO CLASSI:" + importOnlyClassesCreated.stream().count().toString
    println(str)
    val rule = ArchRuleDefinition.noClasses()
      .that
      .resideInAPackage(ENTITIES_PACKAGE)
      .should.dependOnClassesThat.resideInAnyPackage(
        APPLICATIONS_PACKAGE,
        INFRASTRUCTURES_PACKAGE,
        USER_INTERACTIONS_PACKAGE
      )

    rule.check(importOnlyClassesCreated)

  @Test
  def checkApplicationDependencies(): Unit =
    val rule = ArchRuleDefinition.classes()
      .that
      .resideInAnyPackage(APPLICATIONS_PACKAGE)
      .should.onlyHaveDependentClassesThat.resideInAnyPackage(ENTITIES_PACKAGE, APPLICATIONS_PACKAGE)
      .andShould.onlyAccessClassesThat().resideInAnyPackage(
        INFRASTRUCTURES_PACKAGE,
        USER_INTERACTIONS_PACKAGE,
        APPLICATIONS_PACKAGE
      )

    rule.check(importOnlyClassesCreated)
