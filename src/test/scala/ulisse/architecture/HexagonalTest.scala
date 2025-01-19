package ulisse.architecture

import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.Architectures
import org.scalatest.flatspec.AnyFlatSpec
import ulisse.architecture.ArchUnits.IMPORT_ONLY_CLASSES_CREATED

class HexagonalTest extends AnyFlatSpec:

  private def dependenciesRuleOfPackage(rootPackage: String)(noDepend: String*)(depend: String*): ArchRule =
    ArchRuleDefinition
      .noClasses
      .that
      .resideInAPackage(rootPackage)
      .should.dependOnClassesThat.resideInAnyPackage(noDepend: _*)
      .andShould.dependOnClassesThat.resideInAnyPackage(depend: _*)
      .allowEmptyShould(true)

  "no classes of the entities package" should "depends on the applications, infrastructures and userInteractions packages" in:
    val rule = dependenciesRuleOfPackage(Packages.ENTITIES)(
      Packages.APPLICATIONS,
      Packages.INFRASTRUCTURES,
      Packages.USER_INTERACTIONS
    )()

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "no classes of the applications package" should "depend on the infrastructures and userInteractions packages " +
    "and should depend on entities package" in:
      val rule = dependenciesRuleOfPackage(Packages.APPLICATIONS)(Packages.INFRASTRUCTURES, Packages.USER_INTERACTIONS)(
        Packages.ENTITIES
      )

      rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "no classes of the infrastructures package" should "depend on the entities and userInteractions packages " +
    "and should depend on applications package" in:
      val rule = dependenciesRuleOfPackage(Packages.INFRASTRUCTURES)(Packages.USER_INTERACTIONS)(
        Packages.APPLICATIONS,
        Packages.ENTITIES
      )

      rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "no classes of the userInteractions package" should "depend on the entities and infrastructures packages " +
    "and should depend on application package" in:
      val rule = dependenciesRuleOfPackage(Packages.USER_INTERACTIONS)(Packages.INFRASTRUCTURES)(
        Packages.APPLICATIONS,
        Packages.ENTITIES
      )

      rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "hexagonal architecture" should "be entities -> applications -> infrastructures/userInteractions" in:
    val rule = Architectures
      .onionArchitecture
      .domainModels(Packages.ENTITIES)
      .applicationServices(Packages.APPLICATIONS)
      .adapter("infrastructures", Packages.INFRASTRUCTURES)
      .adapter("userInteractions", Packages.USER_INTERACTIONS)
      .allowEmptyShould(true)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)
