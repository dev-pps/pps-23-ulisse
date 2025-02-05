package ulisse.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
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

  "no classes of the entities package" should "depends on the applications, adapters and infrastructures packages" in:
    val rule = dependenciesRuleOfPackage(Packages.ENTITIES)(
      Packages.APPLICATIONS,
      Packages.ADAPTERS,
      Packages.INFRASTRUCTURES
    )()

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "no classes of the applications package" should "depend on the adapters and infrastructures packages " +
    "and should depend on entities package" in:
      val rule = dependenciesRuleOfPackage(Packages.APPLICATIONS)(Packages.ADAPTERS, Packages.INFRASTRUCTURES)(
        Packages.ENTITIES
      )

      rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "classes of the adapters package" should "depend on the entities, applications and infrastructures packages" in:
    val rule = dependenciesRuleOfPackage(Packages.ADAPTERS)()(
      Packages.ENTITIES,
      Packages.APPLICATIONS,
      Packages.INFRASTRUCTURES
    )

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "classes of the infrastructures package" should "depend on the entities, applications and adapters packages" in:
    val rule = dependenciesRuleOfPackage(Packages.INFRASTRUCTURES)()(
      Packages.ENTITIES,
      Packages.APPLICATIONS,
      Packages.ADAPTERS
    )

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "hexagonal architecture" should "be entities -> applications -> adapters" in:
    val rule = Architectures
      .onionArchitecture
      .domainModels(Packages.ENTITIES)
      .applicationServices(Packages.APPLICATIONS)
      .adapter("adapters", Packages.ADAPTERS)
      .ignoreDependency(DescribedPredicate.alwaysTrue(), resideInAPackage(Packages.INFRASTRUCTURES))
      .ignoreDependency(resideInAPackage(Packages.INFRASTRUCTURES), DescribedPredicate.alwaysTrue())
      .allowEmptyShould(true)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)
