package infrastructures.ui.train

import applications.train.{TrainManagers, TrainPorts}
import entities.train.Technology

object TryTrainEditor extends App:
  val manager = TrainManagers.TrainManager(List.empty)
  List(Technology("Normal", 100), Technology("Highspeed", 460)).foreach(
    manager.addTechnology
  )
  manager.createTrain("TR-700", technologyName = "Normal", wagonTypeName = "Other", wagonCapacity = 5, wagonCount = 12)
  manager.createTrain(
    "TR-900",
    technologyName = "Highspeed",
    wagonTypeName = "Passenger",
    wagonCapacity = 50,
    wagonCount = 3
  )

  val trainPort = TrainPorts.BaseInBoundPort(manager)
  TrainEditorView(trainPort)
