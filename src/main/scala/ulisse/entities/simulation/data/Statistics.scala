package ulisse.entities.simulation.data

import ulisse.utils.Times.Time

object Statistics:
  extension (simulationData: SimulationData)
    def cumulativeDelay: Time =
      Time.secondsToOverflowTime(
        simulationData.simulationEnvironment
          .timetables
          .flatMap(_.currentDelay)
          .map(_.toSeconds).sum
      )