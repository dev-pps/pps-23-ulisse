package ulisse.infrastructures.view.timetable

import ulisse.infrastructures.view.timetable.model.TimetableGUIModel.{generateMockTimetable, TimetableEntry}

trait TimetableViewController:
  def insertStation(stationName: String, waitTime: Option[Int]): Unit
  def undoLastInsert(): Unit
  def trainNames: List[String]
  def save(): Unit
  def insertedStations(): List[TimetableEntry]

object TimetableViewController:
  def apply(): TimetableViewController =
    // Here pass input port
    ViewControllerImpl()

  private class ViewControllerImpl extends TimetableViewController:
    override def insertStation(stationName: String, waitTime: Option[Int]): Unit = println("insert station")
    override def undoLastInsert(): Unit                                          = println("undoLastInsert")
    override def trainNames: List[String]                                        = List("Rv-3908", "AV-1000", "RV-2020")
    override def save(): Unit                                                    = println("insert station")
    override def insertedStations(): List[TimetableEntry]                        = generateMockTimetable(10)
