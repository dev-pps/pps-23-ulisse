final case class Model():

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var _stationMap: StationMap = StationMap(List[Station]())

  def stationMap: Seq[Station] = _stationMap.stations
  def addStation(station: Station): Unit =
//    _stationMap = _stationMap.copy(stations = station +: _stationMap.stations)
    _stationMap = StationMap(station +: _stationMap.stations)
    println(_stationMap)
