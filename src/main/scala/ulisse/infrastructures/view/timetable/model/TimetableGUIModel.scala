package ulisse.infrastructures.view.timetable.model

import scala.util.Random

object TimetableGUIModel:
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

  def randomTime(): String = {
    val hour   = Random.nextInt(24)
    val minute = Random.nextInt(60)
    s"$hour:$minute"
  }

  def generateMockTimetable(size: Int): List[TimetableEntry] = {
    (1 to size).map { i =>
      new TimetableEntry {
        val name                          = s"Station $i"
        val arrivingTime: Option[String]  = Some(randomTime())
        val departureTime: Option[String] = Some(randomTime())
        val waitMinutes: Option[Int]      = Option.when(Random.nextBoolean())(Random.nextInt(30))
      }
    }.toList
  }
