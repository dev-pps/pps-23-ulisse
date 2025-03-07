package ulisse.infrastructures.view.timetable

import ulisse.entities.timetable.Timetables.Timetable

/** Objects containing all data models used by timetable views and
  * utility method to convert service application data type into view one.
  */
object TimetableViewModel:

  trait TimetableEntry:
    def name: String
    def arrivingTime: Option[String]
    def departureTime: Option[String]
    def waitMinutes: Option[Int]

    override def toString: String =
      s"(Station: $name, arrivingAt: $arrivingTime, departingAt: $departureTime, waits: $waitMinutes min)"

  /** Timetable station entry information. */
  case class TableEntryData(
      name: String,
      arrivingTime: Option[String],
      departureTime: Option[String],
      waitMinutes: Option[Int]
  ) extends TimetableEntry

  /** Returns converted station info from entries of timetable `t`. */
  extension (t: Timetable)
    def toTimetableEntries: List[TimetableEntry] =
      extension [T](t: Option[T])
        private def optionString: Option[String] = t.map(_.toString)

      t.table.map((station, times) =>
        TableEntryData(
          station.name,
          times.stationTime.arriving.optionString,
          times.stationTime.departure.optionString,
          times.stationTime.waitTime
        )
      ).toList
