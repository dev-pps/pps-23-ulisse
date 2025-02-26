package ulisse.infrastructures.view.timetable.model

import scala.util.Random

object TimetableGUIModel:
  trait TimetableEntry:
    def name: String
    def arrivingTime: String  // Option
    def departureTime: String // Option
    def waitMinutes: Option[Int]

  val mockedData: TableEntryData = TableEntryData("Station A", "9:00", "9:00", Some(3))

  def randomTime(): String = {
    val hour   = Random.nextInt(24)
    val minute = Random.nextInt(60)
    s"$hour:$minute"
  }

  def generateMockTimetable(size: Int): List[TimetableEntry] = {
    (1 to size).map { i =>
      new TimetableEntry {
        val name          = s"Station $i"
        val arrivingTime  = randomTime()
        val departureTime = randomTime()
        val waitMinutes   = if (Random.nextBoolean()) Some(Random.nextInt(30)) else None
      }
    }.toList
  }

  case class TableEntryData(name: String, arrivingTime: String, departureTime: String, waitMinutes: Option[Int])
      extends TimetableEntry
