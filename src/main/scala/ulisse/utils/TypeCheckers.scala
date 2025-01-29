package ulisse.utils

import scala.reflect.ClassTag

object TypeCheckers:

  extension [A, B](a: A)
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    def identicalClassIs(b: B): Boolean = a.getClass == b.getClass

  extension [A: ClassTag, B: ClassTag](a: A)
    def hasCommonSupertype(b: B)(using class1: ClassTag[A], class2: ClassTag[B]): Boolean =
      val runtimeClass1 = class1.runtimeClass
      val runtimeClass2 = class2.runtimeClass
      runtimeClass1.isAssignableFrom(runtimeClass2) || runtimeClass2.isAssignableFrom(runtimeClass1)

    def hasMultipleCommonSupertypes(b: B)(using class1: ClassTag[A], class2: ClassTag[B]): Boolean =
      val class1BaseClasses = class1.runtimeClass.getSuperclass.getClasses.toList
      val class2BaseClasses = class2.runtimeClass.getSuperclass.getClasses.toList
      val commonBaseClasses = class1BaseClasses.intersect(class2BaseClasses)
      commonBaseClasses.sizeIs > 1
