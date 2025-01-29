package ulisse.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.utils.TypeCheckers.*

class IdenticalClass extends AnyFlatSpec with Matchers:
  trait A extends C
  trait B extends A
  trait C
  trait D

  case class AA() extends A
  case class BB() extends B
  case class CC() extends C
  case class DD() extends D

  "check A identical type B" should "be false" in:
    val a: A = AA()
    val b: B = BB()
    a identicalClass b must be(false)

  "check A identical type C" should "be false" in:
    val a: A = AA()
    val c: B = BB()
    a identicalClass c must be(false)

  "check A identical type D" should "be false" in:
    val a: A = AA()
    val d: B = BB()
    a identicalClass d must be(false)

  "check A identical type A" should "be true" in:
    val a1: A = AA()
    val a2: A = AA()
    a1 identicalClass a2 must be(true)
