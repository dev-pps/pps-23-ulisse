//package applications.station.ports
//
//object StationPortInbound:
//  trait AppPortInbound:
//    def start(): Unit
//
//object StationPortOutbound:
//  trait AppPortOutbound:
//    def start(): Unit
////inbound port
//final case class Manager() extends StationPortInbound:
//  private var stationMap: Map[String, Station] = Map.empty
//
//  def start(): Unit = ???
//  // stuff
//  def addStation(): Unit = ???
//  // stuff
//  def removeStation(): Unit = ???
//  // stuff
//  def findStationAt(): Unit = ???
//  // stuff
//
//trait AppPortInbound:
//  def start(): Unit
//
//trait StationPortInbound:
//  def addStation(): Unit
//  def removeStation(): Unit
//  def findStationAt(): Unit
//
////outbound port
//trait StationPortOutbound:
//  def show(): Unit
//
////station -> ui
//case class adapterStationUI(UIPortInbound: UIPortInbound)
//    extends StationPortOutbound:
//  def show(): Unit = UIPortInbound.display()
//
////ui -> station
//case class adapterUIStation(StationPortInbound: StationPortInbound)
//    extends UIPortOutbound:
//  def startAPP(): Unit =
//    // stuff
//    StationPortInbound.start()
//  def addStationToAPP(): Unit       = StationPortInbound.addStation()
//  def removeStationFromAPP(): Unit  = StationPortInbound.removeStation()
//  def findStationAtPorcoDio(): Unit = StationPortInbound.findStationAt()
//
////inbound port
//trait UIPortOutbound:
//  def startAPP(): Unit
//  def addStationToAPP(): Unit
//  def removeStationFromAPP(): Unit
//  def findStationAtPorcoDio(): Unit
//
////outbound port
//trait UIPortInbound:
//  def display(): Unit
