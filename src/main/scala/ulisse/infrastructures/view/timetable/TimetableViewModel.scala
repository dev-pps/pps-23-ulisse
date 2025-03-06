package ulisse.infrastructures.view.timetable

import ulisse.entities.timetable.Timetables.Timetable

object TimetableViewModel:
  opaque type TrainId = String

  def trainId(id: String): TrainId = id
  extension (ti: TrainId)
    def value: String = ti

  trait TimetableEntry:
    def name: String
    def arrivingTime: Option[String]
    def departureTime: Option[String]
    def waitMinutes: Option[Int]

    override def toString: String =
      s"(Station: $name, arrivingAt: $arrivingTime, departingAt: $departureTime, waits: $waitMinutes min)"

  case class TableEntryData(
      name: String,
      arrivingTime: Option[String],
      departureTime: Option[String],
      waitMinutes: Option[Int]
  ) extends TimetableEntry

  extension (t: Timetable)
    def toTimetableEntries: List[TimetableEntry] =
      extension [T](t: Option[T])
        private def optionString(using ev: T <:< AnyRef): Option[String] = t.map(_.toString)

      t.table.map((station, times) =>
        TableEntryData(
          station.name,
          times.stationTime.arriving.optionString,
          times.stationTime.departure.optionString,
          times.stationTime.waitTime
        )
      ).toList

  import scala.util.Random
  private def randomTime(): String =
    val hour   = Random.nextInt(24)
    val minute = Random.nextInt(60)
    s"$hour:$minute"

  def generateMockTimetable(size: Int): List[TimetableEntry] =
    (1 to size).map(i =>
      new TimetableEntry {
        val name                          = s"Station $i"
        val arrivingTime: Option[String]  = Some(randomTime())
        val departureTime: Option[String] = Some(randomTime())
        val waitMinutes: Option[Int]      = Option.when(Random.nextBoolean())(Random.nextInt(30))
      }
    ).toList
