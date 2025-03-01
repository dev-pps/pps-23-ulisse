package ulisse.entities.simulation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import alice.tuprolog.*
import ulisse.entities.Coordinate
import ulisse.entities.station.Station
import scala.language.dynamics

import java.util


class RailwayEnvironmentTest extends AnyWordSpec with Matchers{
  trait MyTrait

  class DynamicMyTrait(val original: MyTrait) extends Dynamic {
    private val dynamicMethods = scala.collection.mutable.Map[String, () => Any]()

    def addMethod[A](name: String, implementation: () => A): Unit = {
      dynamicMethods(name) = implementation
    }

    def selectDynamic(methodName: String): Any = {
      dynamicMethods.get(methodName) match {
        case Some(method) => method()
//        case None => throw new NoSuchMethodException(s"Method $methodName not found")
      }
    }
    def applyDynamic(methodName: String): Any = {
      dynamicMethods.get(methodName) match {
        case Some(method) => method()
//        case None => throw new NoSuchMethodException(s"Method $methodName not found")
      }
    }
  }

  // Usage
  val instance = new MyTrait{}
  val dynamic = new DynamicMyTrait(instance)

  dynamic.addMethod("newDynamicMethod", () => println("Dynamic method called"))
  dynamic.newDynamicMethod


}
//    val stationA = Station("A", Coordinate(0, 0), 1)
