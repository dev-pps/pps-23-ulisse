package ulisse.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.utils.Errors.BaseError

class ValidationUtilsTest extends AnyWordSpec with Matchers:

  case class TestError() extends BaseError
  "ValidationUtils" should:
    "validate a value within the range correctly" in:
      val lowerBound = 0
      val upperBound = 10

      ValidationUtils.validateRange(lowerBound + 1, lowerBound, upperBound, TestError()) shouldBe Right(lowerBound + 1)
      ValidationUtils.validateRange(upperBound - 1, lowerBound, upperBound, TestError()) shouldBe Right(upperBound - 1)
      ValidationUtils.validateRange(lowerBound, lowerBound, upperBound, TestError()) shouldBe Right(lowerBound)
      ValidationUtils.validateRange(upperBound, lowerBound, upperBound, TestError()) shouldBe Right(upperBound)
      ValidationUtils.validateRange(lowerBound - 1, lowerBound, upperBound, TestError()) shouldBe Left(TestError())
      ValidationUtils.validateRange(upperBound + 1, lowerBound, upperBound, TestError()) shouldBe Left(TestError())

    "validate non-negative values correctly" in:
      ValidationUtils.validateNonNegative(5, TestError()) shouldBe Right(5)
      ValidationUtils.validateNonNegative(0, TestError()) shouldBe Right(0)
      ValidationUtils.validateNonNegative(-1, TestError()) shouldBe Left(TestError())

    "validate positive values correctly" in:
      ValidationUtils.validatePositive(5, TestError()) shouldBe Right(5)
      ValidationUtils.validatePositive(0, TestError()) shouldBe Left(TestError())
      ValidationUtils.validatePositive(-1, TestError()) shouldBe Left(TestError())

    "validate non-blank strings correctly" in:
      ValidationUtils.validateNonBlankString("Hello", TestError()) shouldBe Right("Hello")
      ValidationUtils.validateNonBlankString("", TestError()) shouldBe Left(TestError())
      ValidationUtils.validateNonBlankString(" ", TestError()) shouldBe Left(TestError())

    "validate unique items correctly" in:
      ValidationUtils.validateUniqueItems(Seq(1, 2, 3), TestError()) shouldBe Right(Seq(1, 2, 3))
      ValidationUtils.validateUniqueItems(Seq(1, 2, 2), TestError()) shouldBe Left(TestError())
    
    "validate unique items by correctly" in:
      val items = Seq((1, "a"), (1, "b"))
      ValidationUtils.validateUniqueItemsBy(items, identity, TestError()) shouldBe Right(items)
      ValidationUtils.validateUniqueItemsBy(items, _._2, TestError()) shouldBe Right(items)
      ValidationUtils.validateUniqueItemsBy(items, _._1, TestError()) shouldBe Left(TestError())