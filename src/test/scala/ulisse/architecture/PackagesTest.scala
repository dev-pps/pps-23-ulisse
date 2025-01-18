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

  "classes of useCases package" should "have Manager as the ending in the name" in:
    val managerEndingName = "Manager"
    val rule              = simpleEndingNameRule(Packages.USE_CASES, managerEndingName)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "classes of ports package" should "have Port as the ending in the name" in:
    val inputPortEndingName  = "InputPort"
    val outputPortEndingName = "OutputPort"
    val rule                 = doubleEndingNameRulePossible(Packages.PORTS, inputPortEndingName, outputPortEndingName)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "classes of applications.adapters package" should "have Adapter as the ending in the name" in:
    val adapterEndingName = "Adapter"
    val rule              = simpleEndingNameRule(Packages.INPUT_ADAPTER, adapterEndingName)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "classes of infrastructures.adapters package" should "have Adapter as the ending in the name" in:
    val adapterEndingName = "Adapter"
    val rule              = simpleEndingNameRule(Packages.OUTPUT_ADAPTER, adapterEndingName)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)
