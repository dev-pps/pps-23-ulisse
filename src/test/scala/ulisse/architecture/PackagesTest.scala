package ulisse.architecture

import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.scalatest.flatspec.AnyFlatSpec
import ulisse.architecture.ArchUnits.IMPORT_ONLY_CLASSES_CREATED

class PackagesTest extends AnyFlatSpec:

  private val simpleEndingNameRule: (String, String) => ArchRule = (rootPackage, endingName) =>
    ArchRuleDefinition
      .classes
      .that
      .resideInAPackage(rootPackage)
      .should.haveSimpleNameEndingWith(endingName)
      .allowEmptyShould(true)

  private val doubleEndingNameRulePossible: (String, String, String) => ArchRule =
    (rootPackage, firstEnding, orSecondEnding) =>
      ArchRuleDefinition
        .classes
        .that
        .resideInAPackage(rootPackage)
        .should.haveSimpleNameEndingWith(firstEnding)
        .orShould.haveSimpleNameEndingWith(orSecondEnding)
        .allowEmptyShould(true)

  private val tripleEndingNameRulePossible: (String, String, String, String) => ArchRule =
    (rootPackage, firstEnding, orSecondEnding, orThirdEnding) =>
      ArchRuleDefinition
        .classes
        .that
        .resideInAPackage(rootPackage)
        .should.haveSimpleNameEndingWith(firstEnding)
        .orShould.haveSimpleNameEndingWith(orSecondEnding)
        .orShould.haveSimpleNameEndingWith(orThirdEnding)
        .allowEmptyShould(true)

  "classes of ports package" should "have Port as the ending in the name" in:
    val portEndingName       = "Ports"
    val inputPortEndingName  = "Input"
    val outputPortEndingName = "Output"
    val rule = tripleEndingNameRulePossible(Packages.PORTS, portEndingName, inputPortEndingName, outputPortEndingName)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "classes of managers package" should "have Manager as the ending in the name" in:
    val managerEndingName  = "Manager"
    val managersEndingName = "Managers"
    val rule               = doubleEndingNameRulePossible(Packages.MANAGERS, managersEndingName, managerEndingName)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "classes of useCases package" should "have Manager as the ending in the name" in:
    val serviceEndingName  = "Service"
    val servicesEndingName = "Services"
    val rule               = doubleEndingNameRulePossible(Packages.USE_CASES, servicesEndingName, serviceEndingName)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "classes of adapters package" should "have Adapter as the ending in the name" in:
    val adapterEndingName = "Adapter"
    val rule              = simpleEndingNameRule(Packages.ADAPTERS, adapterEndingName)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)
