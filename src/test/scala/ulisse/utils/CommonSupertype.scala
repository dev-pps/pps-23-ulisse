package ulisse.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.utils.TypeCheckers.hasCommonSupertype

class CommonSupertype extends AnyFlatSpec with Matchers:
  trait A extends C
  trait B extends A
  trait C
  trait D

  "check A common trait B" should "be true" in:
    val a: A = new A {}
    val b: B = new B {}
    a hasCommonSupertype b must be(true)

  "check A common trait C" should "be true" in:
    val a: A = new A {}
    val c: C = new C {}
    a hasCommonSupertype c must be(true)

  "check A common trait D" should "be false" in:
    val a: A = new A {}
    val d: D = new D {}
    a hasCommonSupertype d must be(false)

  "check A common trait A" should "be true" in:
    val a1: A = new A {}
    val a2: A = new A {}
    a1 hasCommonSupertype a2 must be(true)

//  "check A multiple common trait B" should "be false" in:
//    val a: A = new A {}
//    val b: B = new B {}
//    a hasMultipleCommonSupertypes b must be(false)
