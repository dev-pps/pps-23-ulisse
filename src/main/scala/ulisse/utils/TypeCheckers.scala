package ulisse.utils

import scala.reflect.{ClassTag, TypeTest, Typeable}

object TypeCheckers:

  // eq stesso riferimento
  //

  extension [A, B](a: A)
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    def isIdenticalClass(b: B): Boolean = a.getClass == b.getClass

  extension [A: ClassTag, B: ClassTag](a: A)
    def hasCommonSupertype(b: B): Boolean =
      val class1 = implicitly[ClassTag[A]].runtimeClass
      val class2 = implicitly[ClassTag[B]].runtimeClass
      class1.isAssignableFrom(class2) || class2.isAssignableFrom(class1)

//    def hasMultipleCommonSupertypes(b: B)(using class1: ClassTag[A], class2: ClassTag[B]): Boolean =
//      val class1BaseClasses = class1.runtimeClass.getSuperclass.getClasses.toList
//      val class2BaseClasses = class2.runtimeClass.getSuperclass.getClasses.toList
//      val commonBaseClasses = class1BaseClasses.intersect(class2BaseClasses)
//      commonBaseClasses.sizeIs > 1
