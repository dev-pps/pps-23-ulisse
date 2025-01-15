package infrastructures.ui.train

import applications.train.{TrainManagers, TrainPorts}
import entities.train.Technology

object TryTrainEditor extends App:
  val manager = TrainManagers.TrainManager(List.empty)
  List(Technology("Normal", 100), Technology("Highspeed", 460)).foreach(
    manager.addTechnology
  )
  val trainPort = TrainPorts.BaseInBoundPort(manager)
  TrainEditorView(trainPort)
