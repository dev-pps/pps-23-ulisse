package ulisse.architecture

import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.Architectures
import org.scalatest.flatspec.AnyFlatSpec
import ulisse.architecture.ArchUnits.IMPORT_ONLY_CLASSES_CREATED

class HexagonalTest extends AnyFlatSpec:

  "no classes of the entities package" should "depends on the applications, infrastructures and userInteractions packages" in:
    val rule = ArchRuleDefinition.noClasses()
      .that
      .resideInAPackage(Packages.ENTITIES)
      .should.dependOnClassesThat.resideInAnyPackage(
        Packages.APPLICATIONS,
        Packages.INFRASTRUCTURES,
        Packages.USER_INTERACTIONS
      )
    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "no classes of the applications package" should "depend on the infrastructures and userInteractions packages " +
    "and should depend on entities package" in:
      val rule = ArchRuleDefinition.noClasses()
        .that
        .resideInAnyPackage(Packages.APPLICATIONS)
        .should.dependOnClassesThat.resideInAnyPackage(Packages.INFRASTRUCTURES, Packages.USER_INTERACTIONS)
        .andShould.dependOnClassesThat.resideInAnyPackage(Packages.ENTITIES)
      rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "no classes of the infrastructures package" should "depend on the entities and userInteractions packages " +
    "and should depend on applications package" in:
      val rule = ArchRuleDefinition.noClasses()
        .that
        .resideInAnyPackage(Packages.INFRASTRUCTURES)
        .should.dependOnClassesThat.resideInAnyPackage(Packages.ENTITIES, Packages.USER_INTERACTIONS)
        .andShould.dependOnClassesThat.resideInAnyPackage(Packages.APPLICATIONS)

      rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "no classes of the userInteractions package" should "depend on the entities and infrastructures packages " +
    "and should depend on application package" in:
      val rule = ArchRuleDefinition.noClasses()
        .that
        .resideInAnyPackage(Packages.USER_INTERACTIONS)
        .should.dependOnClassesThat.resideInAnyPackage(Packages.ENTITIES, Packages.INFRASTRUCTURES)
        .andShould.dependOnClassesThat.resideInAnyPackage(Packages.APPLICATIONS)
        .allowEmptyShould(true)

      rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "hexagonal architecture" should "be entities -> applications -> infrastructures/userInteractions" in:
    val rule = Architectures.onionArchitecture()
      .domainModels(Packages.ENTITIES)
      .applicationServices(Packages.APPLICATIONS)
      .adapter("infrastructures", Packages.INFRASTRUCTURES)
      .adapter("userInteractions", Packages.USER_INTERACTIONS)
      .allowEmptyShould(true)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)
