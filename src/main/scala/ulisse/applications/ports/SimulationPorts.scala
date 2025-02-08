package ulisse.applications.ports

object SimulationPorts:
  trait Output:
    def stepNotification(): Unit

  trait Input:
    def start(): Unit
    def stop(): Unit
    def reset(): Unit
