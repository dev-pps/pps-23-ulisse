package ulisse.dsl

import scala.language.dynamics

object RailwayEnvironment extends App:


  class DynamicStationBuilder(val original: Stations) extends Dynamic with Stations(original.values) {
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
  //  val instance = new StationBuilder {}
  //  val dynamic = new DynamicStationBuilder(instance)
  //
  //  dynamic.addMethod("newDynamicMethod", () => println("Dynamic method called"))
  //  dynamic.newDynamicMethod


  trait El
  case class Station(name: String)

  // Define the Stations class with companion object
  trait Stations (val values: Seq[Station])

  // Companion object with apply method that accepts a block
  object Stations:
    def apply(stations: Station*): DynamicStationBuilder =
      val dinamicSb = new DynamicStationBuilder(new Stations(stations){})
      stations.foreach(station =>
        println(s"adding method: ${station.name}")
        dinamicSb.addMethod(station.name, () => station))
      dinamicSb

  case class Route(name: String)

  // Define the Stations class with companion object
  class Routes private(val values: Seq[Route]) extends El

  // Companion object with apply method that accepts a block
  object Routes:
    def apply(routes: Route*): Routes = new Routes(routes)

  // Define the Stations class with companion object
  class Env private(val station: DynamicStationBuilder, val routes: Routes)

  // Companion object with apply method that accepts a block
  object Env:
    def apply(station: DynamicStationBuilder, routes: Routes): Env = new Env(station, routes)

  val stationsA = Stations(
    Station("A"),
    Station("B")
  )

  stationsA.D
  println(stationsA.A)
  val routes = Routes{
    Route("A")
    Route("B")
  }
  //  stations.A

  val env = Env(
    Stations(
      Station("A"),
      Station("B")
    ),
    Routes(
      Route("A"),
      Route("B")
    )
  )


  println(stationsA.values)

