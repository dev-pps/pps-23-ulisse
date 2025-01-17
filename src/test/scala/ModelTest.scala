//import applications.station.Manger
//import entities.Location
//import entities.station.Station
//import org.scalatest.matchers.should.Matchers
//import org.scalatest.wordspec.AnyWordSpec
//
//class ModelTest extends AnyWordSpec with Matchers:
//
//  "Model" should:
//    "initialize with an empty station map" in:
//      Manger().stationMap shouldBe empty
//
//    "add a station to the station map" in:
//      val model   = Manger()
//      val station = Station("Station1", Location(1, 1), 1)
//      model.addStation(station)
//      model.stationMap should contain only station
//
//    "not allow duplicate stations by location" in:
//      val model    = Manger()
//      val station1 = Station("Station1", Location(1, 1), 1)
//      val station2 = Station("Station2", Location(1, 1), 1)
//
//      model.addStation(station1)
//      intercept[IllegalArgumentException]:
//        model.addStation(station2)
//
//      model.stationMap should have size 1
//      model.stationMap should contain only station1
//
//    "not allow duplicate stations by name" in:
//      val model    = Manger()
//      val station1 = Station("Station1", Location(1, 1), 1)
//      val station2 = Station("Station1", Location(2, 2), 1)
//
//      model.addStation(station1)
//      intercept[IllegalArgumentException]:
//        model.addStation(station2)
//
//      model.stationMap should have size 1
//      model.stationMap should contain only station1
//
//    "remove a station from the station map" in:
//      val model    = Manger()
//      val station1 = Station("Station1", Location(1, 1), 1)
//      val station2 = Station("Station2", Location(2, 2), 1)
//
//      model.addStation(station1)
//      model.addStation(station2)
//      model.removeStation(station1)
//
//      model.stationMap should contain only station2
//
//    "handle removing a station that does not exist" in:
//      val model    = Manger()
//      val station1 = Station("Station1", Location(1, 1), 1)
//      val station2 = Station("Station2", Location(2, 2), 1)
//
//      model.addStation(station1)
//      model.removeStation(station2)
//
//      model.stationMap should contain only station1
//
//    "find a station at a specific location" in:
//      val model    = Manger()
//      val location = Location(1, 1)
//      val station  = Station("Station", location, 1)
//      model.addStation(station)
//
//      model.findStationAt(location) shouldBe Some(station)
//      model.findStationAt(Location(0.0, 0.0)) shouldBe None
