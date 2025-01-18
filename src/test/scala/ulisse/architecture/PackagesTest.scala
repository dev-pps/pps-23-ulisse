package ulisse.architecture

import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.scalatest.flatspec.AnyFlatSpec
import ArchUnits.IMPORT_ONLY_CLASSES_CREATED

class PackagesTest extends AnyFlatSpec:

  "classes of use case package" should "have Manager as the ending in the name" in:
    val managerEndingName = "Manager"
    val rule = ArchRuleDefinition.classes
      .that
      .resideInAPackage(Packages.USE_CASES)
      .should.haveSimpleNameContaining(managerEndingName)

    rule.check(IMPORT_ONLY_CLASSES_CREATED)
