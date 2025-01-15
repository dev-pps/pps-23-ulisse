package infrastructures.ui.train

import applications.train.{TrainManager, TrainPorts}
import entities.train.Technology

object TryTrainEditor extends App:
  val service = TrainManager.TrainService(List.empty)
  List(Technology("Normal", 100), Technology("Highspeed", 460)).foreach(
    service.addTechnology
  )

  val trainPort = TrainPorts.BaseInBoundPort(service)
  TrainEditorView(trainPort)
