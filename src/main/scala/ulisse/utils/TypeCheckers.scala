package ulisse.utils

import scala.annotation.targetName
import scala.reflect.{ClassTag, Typeable}

object TypeCheckers:

  extension [A: Typeable](a: A)
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    def identicalClass[B: Typeable](b: B): Boolean = a.getClass == b.getClass
