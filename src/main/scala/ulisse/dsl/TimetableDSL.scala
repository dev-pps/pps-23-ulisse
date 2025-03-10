package ulisse.dsl

import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.RouteType.Normal
import ulisse.entities.station.Station
import ulisse.entities.timetable.Timetables.{RailInfo, Timetable, TimetableBuilder, WaitTime}
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons
import ulisse.utils.Times.ClockTime
import ulisse.utils.Times.FluentDeclaration.h

import scala.language.postfixOps

/** Timetable creation DSL.
  *
  * Provide a set of extension method to create in a more natural way timetable.
  *
  *  After the declaration of train, departure time and station then
  *  you have to declare `RailInfo` and the station to reach by `andStopIn` or `travelsTo`.
  *
  *  After `andStopIn` you have to say how many minutes train stops and then you can continue.
  *
  *  To finalize building of [[Timetable]] just call `arrivesTo` followed by last station.
  *
  * A usage example:
  *
  * {{{trainRV35 at h(9).m(30).getOrDefault startFrom stationA travelOn
  * railAB andStopIn stationB waitingForMinutes 5 travelOn
  * railAB travelsTo stationB arrivesTo stationA}}}
  */
object TimetableDSL:

  case class InitTable(train: Train, departureTime: ClockTime):
    def startFrom(startStation: Station): TimetableBuilder = TimetableBuilder(train, startStation, departureTime)

  case class TravelOnRailInfo(railInfo: RailInfo, builder: TimetableBuilder)

  case class TravelRailInfoStation(railInfo: RailInfo, station: Station, builder: TimetableBuilder)

  extension (train: Train)
    def at(departureTime: ClockTime): InitTable = InitTable(train, departureTime)

  extension (builder: TimetableBuilder)
    infix def thenOnRail(railInfo: RailInfo): TravelOnRailInfo = TravelOnRailInfo(railInfo, builder)

  extension (tri: TravelOnRailInfo)
    infix def andStopIn(station: Station): TravelRailInfoStation =
      TravelRailInfoStation(tri.railInfo, station, tri.builder)

    infix def travelsTo(station: Station): TimetableBuilder =
      tri.builder.transitIn(station)(tri.railInfo)

    infix def arrivesTo(station: Station): Timetable =
      tri.builder.arrivesTo(station)(tri.railInfo)

  extension (tris: TravelRailInfoStation)
    infix def waitingForMinutes(waitTime: WaitTime): TimetableBuilder =
      tris.builder.stopsIn(tris.station, waitTime)(tris.railInfo)
