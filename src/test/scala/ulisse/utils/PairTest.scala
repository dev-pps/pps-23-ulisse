package ulisse.utils

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PairTest extends AnyWordSpec with Matchers:
  private val valueA = 1
  private val valueB = 2
  val pair           = Pair(valueA, valueB)

  "Pair" when:
    "create a pair with the values" should:
      "return the pair with the values" in:
        pair.a mustBe valueA
        pair.b mustBe valueB

    "change the values of the pair" should:
      "return the pair with the new first value" in:
        val newA    = 3
        val newPair = pair.withA(newA)
        newPair.a mustBe newA
        newPair.b mustBe valueB
      "return the pair with the new second value" in:
        val valueB  = 4
        val newPair = pair.withB(valueB)
        newPair.a mustBe valueA
        newPair.b mustBe valueB

    "is defined as a numeric pair" should:
      "return the difference of the values" in:
        pair.minus(pair) mustBe Pair(valueA - valueA, valueB - valueB)
      "return the sum of the values" in:
        pair.plus(pair) mustBe Pair(valueA + valueA, valueB + valueB)
      "return the product of the values" in:
        pair.times(pair) mustBe Pair(valueA * valueA, valueB * valueB)
