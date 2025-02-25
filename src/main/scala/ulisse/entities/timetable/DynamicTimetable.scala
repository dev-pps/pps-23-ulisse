package ulisse.entities.timetable

import ulisse.entities.simulation.EnvironmentElements.EnvironmentElement
import ulisse.entities.station.Station
import ulisse.entities.timetable.Timetables.{StationTime, Timetable}

import scala.collection.immutable.ListMap

trait DynamicTimetable extends Timetable with EnvironmentElement:
  def effectiveTable: ListMap[Station, TrainStationTime]
