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

  "check A identical class B" should "be false" in:
    val a: A = AA()
    val b: B = BB()
    a identicalClassIs b must be(false)

  "check A identical class C" should "be false" in:
    val a: A = AA()
    val c: C = CC()
    a identicalClassIs c must be(false)

  "check A identical class D" should "be false" in:
    val a: A = AA()
    val d: D = DD()
    a identicalClassIs d must be(false)

  "check A identical class A" should "be true" in:
    val a1: A = AA()
    val a2: A = AA()
    a1 identicalClassIs a2 must be(true)
