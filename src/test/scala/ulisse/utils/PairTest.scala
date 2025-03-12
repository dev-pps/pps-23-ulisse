package ulisse.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class PairTest extends AnyFlatSpec with Matchers:
  private val valueA = 1
  private val valueB = 2
  val pair           = Pair(valueA, valueB)

  "create a pair" should "return the pair with the values" in:
    pair.a mustBe valueA
    pair.b mustBe valueB

  "change the first value of the pair" should "return the pair with the new first value" in:
    val newA    = 3
    val newPair = pair.withA(newA)
    newPair.a mustBe newA
    newPair.b mustBe valueB

  "change the second value of the pair" should "return the pair with the new second value" in:
    val valueB  = 4
    val newPair = pair.withB(valueB)
    newPair.a mustBe valueA
    newPair.b mustBe valueB
