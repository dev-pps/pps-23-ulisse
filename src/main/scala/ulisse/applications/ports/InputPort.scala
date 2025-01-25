package ulisse.applications.ports

import java.util.concurrent.LinkedBlockingQueue

trait InputPort(using eventStream: LinkedBlockingQueue[?])
