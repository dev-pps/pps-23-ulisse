//package applications.station
//
//import applications.station.StationMap
//import entities.Location
//import entities.Location.Grid
//import entities.station.Station
//
//trait Manger:
//  def stationMap: Seq[Station[Grid]]
//  def addStation(station: Station[Grid]): Unit
//  def removeStation(station: Station[Grid]): Unit
//  def findStationAt(location: Location): Option[Station[Grid]]
//
///** The application model containing the application mutable state.
//  *
//  * @constructor
//  *   create a new Model.
//  */
//object Manger:
//  def apply(): Manger = ManagerImpl()
//  private final case class ManagerImpl() extends Manger:
//
//    @SuppressWarnings(Array("org.wartremover.warts.Var"))
//    private var _stationMap: StationMap[List, Grid] =
//      StationMap[Grid](List[Station[Grid]]())
//
//    /** Retrieves all the stations in the model as a sequence.
//      *
//      * @return
//      *   A sequence of `Station` objects representing the stations currently
//      *   stored in the model.
//      */
//    def stationMap: Seq[Station[Grid]] = _stationMap.stations
//
//    /** Adds a station to the model.
//      *
//      * @param station
//      *   The station to add to the model.
//      * @throws IllegalArgumentException
//      *   If the station name or location is not unique.
//      */
//    def addStation(station: Station[Grid]): Unit =
//      _stationMap = StationMap(station +: _stationMap.stations)
//
//    /** Removes a station from the model. If the station is not part of the
//      * model, nothing happens.
//      * @param station
//      *   The station to remove from the model.
//      */
//    def removeStation(station: Station[Grid]): Unit =
//      val updatedStations =
//        _stationMap.stations.filterNot(_.location == station.location)
//      _stationMap = StationMap(updatedStations)
//
//    /** Searches for a station at the specified location.
//      *
//      * @param location
//      *   The location to search for.
//      * @return
//      *   An `Option[Station]`, which contains the station at the given location
//      *   if found, or `None` if no station exists at that location.
//      */
//    def findStationAt(location: Location): Option[Station[Grid]] =
//      _stationMap.stations.find(_.location == location)
