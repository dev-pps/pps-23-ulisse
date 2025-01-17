package architecture

import com.tngtech.archunit.core.importer.{ClassFileImporter, ImportOption}
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.Architectures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class Hexagonal extends AnyFlatSpec with Matchers:
  private val ENTITIES_PACKAGE          = "..entities.."
  private val APPLICATIONS_PACKAGE      = "..applications.."
  private val INFRASTRUCTURES_PACKAGE   = "..infrastructures.."
  private val USER_INTERACTIONS_PACKAGE = "..userInteractions.."

  private val importedProjectClasses = new ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .importClasspath()

//  val str = "NUMERO CLASSI:" + importOnlyClassesCreated.stream().count().toString

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
//    .withImportOption(location =>
//      println(location)
//      true
//    )
    .importClasspath()

  "no classes of the entities package" should "depends on the applications, infrastructures and userInteractions packages" in:
    val rule = ArchRuleDefinition.noClasses()
      .that
      .resideInAPackage(ENTITIES_PACKAGE)
      .should.dependOnClassesThat.resideInAnyPackage(
        APPLICATIONS_PACKAGE,
        INFRASTRUCTURES_PACKAGE,
        USER_INTERACTIONS_PACKAGE
      )
    rule.check(importOnlyClassesCreated)

  "no classes of the applications package" should "depend on the infrastructures and userInteractions packages " +
    "and should depend on entities package" in:
      val rule = ArchRuleDefinition.noClasses()
        .that
        .resideInAnyPackage(APPLICATIONS_PACKAGE)
        .should.dependOnClassesThat.resideInAnyPackage(INFRASTRUCTURES_PACKAGE, USER_INTERACTIONS_PACKAGE)
        .andShould.dependOnClassesThat.resideInAnyPackage(ENTITIES_PACKAGE)
      rule.check(importOnlyClassesCreated)

  "no classes of the infrastructures package" should "depend on the entities and userInteractions packages " +
    "and should depend on applications package" in:
      val rule = ArchRuleDefinition.noClasses()
        .that
        .resideInAnyPackage(INFRASTRUCTURES_PACKAGE)
        .should.dependOnClassesThat.resideInAnyPackage(ENTITIES_PACKAGE, USER_INTERACTIONS_PACKAGE)
        .andShould.dependOnClassesThat.resideInAnyPackage(APPLICATIONS_PACKAGE)

      rule.check(importOnlyClassesCreated)

  "no classes of the userInteractions package" should "depend on the entities and infrastructures packages " +
    "and should depend on application package" in:
      val rule = ArchRuleDefinition.noClasses()
        .that
        .resideInAnyPackage(USER_INTERACTIONS_PACKAGE)
        .should.dependOnClassesThat.resideInAnyPackage(ENTITIES_PACKAGE, INFRASTRUCTURES_PACKAGE)
        .andShould.dependOnClassesThat.resideInAnyPackage(APPLICATIONS_PACKAGE)
        .allowEmptyShould(true)

      rule.check(importOnlyClassesCreated)

  "hexagonal architecture" should "be entities -> applications -> infrastructures/userInteractions" in:
    val rule = Architectures.onionArchitecture()
      .domainModels(ENTITIES_PACKAGE)
      .applicationServices(APPLICATIONS_PACKAGE)
      .adapter("infrastructures", INFRASTRUCTURES_PACKAGE)
      .adapter("userInteractions", USER_INTERACTIONS_PACKAGE)
      .allowEmptyShould(true)

    rule.check(importOnlyClassesCreated)
