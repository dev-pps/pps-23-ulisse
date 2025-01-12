//package entities.station
//
//import entities.Location
//import Location.*
//
///** Defines a Station. */
//trait Station[L <: Location]:
//  /** The name of the station. Must not be empty or blank. */
//  val name: String
//  require(!name.isBlank, "name must not be empty or blank")
//  val location: L
//
//  /** The number of tracks of the station. Must be greater than 0. */
//  val numberOfTrack: Int
//  require(numberOfTrack > 0, "numberOfTrack must be greater than 0")
//
///** Factory for [[Station]] instances. */
//object Station:
//  def apply[L <: Location](
//      name: String,
//      location: L,
//      numberOfTrack: Int
//  ): Station[L] =
//    StationImpl(name, location, numberOfTrack)
//
///** Defines a Selectable Object. */
//trait Selectable:
//  val selected: Boolean
//
//final case class StationImpl[L <: Location](
//    name: String,
//    location: L,
//    numberOfTrack: Int
//) extends Station[L]
//
//final case class SelectableStation[L <: Location](
//    station: Station[L],
//    selected: Boolean
//) extends Station[L] with Selectable:
//  export station.*
