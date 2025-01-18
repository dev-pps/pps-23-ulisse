package ulisse.architecture

import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.scalatest.flatspec.AnyFlatSpec
import ArchUnits.IMPORT_ONLY_CLASSES_CREATED

class PackagesTest extends AnyFlatSpec:

  "classes of useCases package" should "have Manager as the ending in the name" in:
    val managerEndingName = "Manager"
    val rule = ArchRuleDefinition
      .classes
      .that
      .resideInAPackage(Packages.USE_CASES)
      .should.haveSimpleNameEndingWith(managerEndingName)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "classes of ports package" should "have Port as the ending in the name" in:
    val outputPortEndingName = "OutputPort"
    val inputPortEndingName  = "InputPort"
    val rule = ArchRuleDefinition
      .classes
      .that
      .resideInAPackage(Packages.PORTS)
      .should.haveSimpleNameEndingWith(outputPortEndingName)
      .orShould.haveSimpleNameEndingWith(inputPortEndingName)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)

  "classes of applications.adapters package" should "have Adapter as the ending in the name" in:
    val adapterEndingName = "Adapter"
    val rule = ArchRuleDefinition
      .classes
      .that
      .resideInAPackage(Packages.INPUT_ADAPTER)
      .should.haveSimpleNameEndingWith(adapterEndingName)
      .allowEmptyShould(true)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)
