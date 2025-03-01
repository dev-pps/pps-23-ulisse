package ulisse.dsl
object RailwayEnvironment extends App:
  trait El
  case class Station(name: String)

  // Define the Stations class with companion object
  class Stations private(val values: Seq[Station]) extends El

  // Companion object with apply method that accepts a block
  object Stations:
    def apply(stations: Station*): Stations = new Stations(stations)

  case class Route(name: String)

  // Define the Stations class with companion object
  class Routes private(val values: Seq[Route]) extends El

  // Companion object with apply method that accepts a block
  object Routes:
    def apply(routes: Route*): Routes = new Routes(routes)

  // Define the Stations class with companion object
  class Env private(val values: Seq[El])

  // Companion object with apply method that accepts a block
  object Env:
    def apply(el: El*): Env = new Env(el)

  val stations = Stations{
    Station("A")
    Station("B")
  }
  val routes = Routes{
    Route("A")
    Route("B")
  }

  val env = Env:
    Stations:
      Station("A")
      Station("B")

    Routes:
      Route("A")
      Route("B")


  println(stations.values)

