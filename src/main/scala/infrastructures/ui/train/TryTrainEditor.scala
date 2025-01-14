package infrastructures.ui.train

import applications.train.{TrainManager, TrainPorts}

object TryTrainEditor extends App:
  val model     = TrainManager.TrainService()
  val trainPort = TrainPorts.BaseInBoundPort(model)

  val adapter = TrainEditorAdapter(trainPort)
  TrainEditor(adapter)
