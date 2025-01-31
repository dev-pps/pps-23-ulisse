import cats.effect.IO
import cats.effect.unsafe.IORuntime
import ulisse.applications.adapters.RouteAdapter.UIAdapter
import ulisse.applications.useCases.RouteManager
import ulisse.infrastructures.view.map.MapView

import java.time.Instant
import java.util.concurrent.LinkedBlockingQueue
import scala.annotation.tailrec
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.swing.event.Event

@main def testGUI(): Unit =
  import cats.effect.IO
  import cats.effect.kernel.Ref
  import cats.effect.unsafe.implicits.global

  import scala.swing.*
  import scala.swing.event.ButtonClicked

  // Stato e azioni
  final case class AppState(counter: Int)
  sealed trait Action
  case object Increment extends Action
  case object Decrement extends Action

  // Funzione di aggiornamento dello stato
  def updateState(state: AppState, action: Action): AppState = action match
    case Increment => state.copy(counter = state.counter + 1)
    case Decrement => state.copy(counter = state.counter - 1)

  // Funzione per eseguire operazioni nel thread della GUI
  def runOnEDT(io: IO[Unit]): Unit = Swing.onEDT(io.unsafeRunAndForget())

  // Scala Swing GUI con Cats Effect Ref
  // Stato immutabile gestito con Ref
  val stateRef: Ref[IO, AppState] = Ref.of[IO, AppState](AppState(0)).unsafeRunSync()

  // Avvia l'applicazione Scala Swing
  def top: MainFrame = new MainFrame:
    title = "Esempio Completo GUI con Cats Effect"
    visible = true
    preferredSize = new Dimension(300, 200)

    // Componenti della GUI
    val label           = new Label("Contatore: 0")
    val incrementButton = new Button("Incrementa")
    val decrementButton = new Button("Decrementa")

    // Layout dei componenti
    contents = new BoxPanel(Orientation.Vertical):
      contents += label
      contents += incrementButton
      contents += decrementButton
      border = Swing.EmptyBorder(10, 10, 10, 10)

    // Ascolta gli eventi dei pulsanti
    listenTo(incrementButton, decrementButton)

    // Gestione degli eventi
    reactions += {
      case ButtonClicked(`incrementButton`) =>
        val update = stateRef
          .updateAndGet(state => updateState(state, Increment))
          .flatMap(updatedState => IO(label.text = s"Contatore: ${updatedState.counter}"))
        runOnEDT(update)

      case ButtonClicked(`decrementButton`) =>
        val update = stateRef
          .updateAndGet(state => updateState(state, Decrement))
          .flatMap(updatedState => IO(label.text = s"Contatore: ${updatedState.counter}"))
        runOnEDT(update)
    }

  val app: MainFrame = top

@main def testEngineLoopOnApplication(): Unit =
  val actionQueue = new LinkedBlockingQueue[RouteManager => RouteManager]()

  @tailrec
  def engineLoop(routeManager: RouteManager, lastUpdate: Instant, minInterval: FiniteDuration): Unit =
    val action        = Option(actionQueue.poll()).getOrElse(manager => manager)
    val newState      = action(routeManager)
    val currentTime   = Instant.now()
    val deltaTime     = java.time.Duration.between(lastUpdate, currentTime).toMillis.millis
    val remainingWait = (minInterval - deltaTime).max(Duration.Zero)
    Thread.sleep(minInterval.toMillis)
    engineLoop(newState, currentTime, minInterval)

  val routeManager = RouteManager.empty()
  val UIPort       = UIAdapter(actionQueue)
  val view         = MapView(UIPort)
  val interval     = (1 / 60).second
  // Avvia il loop temporizzato
  engineLoop(routeManager, Instant.now(), interval)

// per lo start stop si fa una incapsulamento sulla simulazione e tutte le altre cose girano
@main def testEngineLoop(): Unit =

  final case class GameState(counter: Int)

  val actionQueue: LinkedBlockingQueue[GameState => GameState] = new LinkedBlockingQueue[GameState => GameState]()

  def defaultUpdateState(state: GameState): GameState = state.copy(counter = state.counter + 1)
  def renderState(state: GameState): IO[Unit]         = IO(println(s"Counter: ${state.counter}"))

  // Funzione che esegue il loop temporizzato
  def engineLoop(state: GameState, lastUpdate: Instant, minInterval: FiniteDuration): IO[Unit] =
    for {
      // Legge un'azione dalla coda o usa una funzione che restituisce lo stato invariato
      action <- IO(Option(actionQueue.poll()).getOrElse(identity[GameState] _))
      newState = action(state) // apply action to state
      // Renderizza lo stato corrente
      _ <- renderState(newState)
      // Aspetta l'intervallo di tempo
      currentTime <- IO(Instant.now())
      deltaTime     = java.time.Duration.between(lastUpdate, currentTime).toMillis.millis
      remainingWait = (minInterval - deltaTime).max(Duration.Zero)
      _ <- IO.sleep(minInterval)
      // Richiama il loop con lo stato aggiornato
      _ <- engineLoop(newState, currentTime, minInterval)
    } yield ()

  new Thread(() => {
    Thread.sleep(3000) // Aspetta 3 secondi
    actionQueue.put(state =>
      println(s"State $state")
      state.copy(counter = state.counter + 10)
    )                  // Aggiunge un'azione
    Thread.sleep(3000) // Aspetta altri 3 secondi
    actionQueue.put(state =>
      println(s"State $state")
      state.copy(counter = state.counter * 2)
    )                  // Aggiunge un'altra azione
    Thread.sleep(3000) // Aspetta altri 3 secondi
  }).start()

  implicit val runtime: IORuntime = IORuntime.global
  val initialState                = GameState(counter = 0)
  val interval                    = 1.second
  // Avvia il loop temporizzato
  engineLoop(initialState, Instant.now(), interval).unsafeRunSync()

@main def testEngineLoopWithoutLib(): Unit =
  final case class GameState(counter: Int)

  val actionQueue: LinkedBlockingQueue[GameState => GameState] = new LinkedBlockingQueue[GameState => GameState]()
  def defaultUpdateState(state: GameState): GameState          = state.copy(counter = state.counter + 1)
  def renderState(state: GameState): Unit                      = IO(println(s"Counter: ${state.counter}"))

  @tailrec
  def engineLoop(state: GameState, lastUpdate: Instant, minInterval: FiniteDuration): Unit =
    val action   = Option(actionQueue.poll()).getOrElse(identity[GameState] _)
    val newState = action(state)
    println(s"Counter: ${newState.counter}")
    val currentTime   = Instant.now()
    val deltaTime     = java.time.Duration.between(lastUpdate, currentTime).toMillis.millis
    val remainingWait = (minInterval - deltaTime).max(Duration.Zero)
    Thread.sleep(minInterval.toMillis)
    engineLoop(newState, currentTime, minInterval)

  new Thread(() => {
    Thread.sleep(3000) // Aspetta 3 secondi
    actionQueue.put(state =>
      println(s"State $state")
      state.copy(counter = state.counter + 10)
    )                  // Aggiunge un'azione
    Thread.sleep(3000) // Aspetta altri 3 secondi
    actionQueue.put(state =>
      println(s"State $state")
      state.copy(counter = state.counter * 2)
    )                  // Aggiunge un'altra azione
    Thread.sleep(3000) // Aspetta altri 3 secondi
  }).start()

  val initialState = GameState(counter = 0)
  val interval     = 1.second

  // Avvia il loop temporizzato
  engineLoop(initialState, Instant.now(), interval)

@main def testEngineLoopWithoutFuture(): Unit =
  final case class GameState(counter: Int)

  val actionQueue: LinkedBlockingQueue[GameState => GameState] = new LinkedBlockingQueue[GameState => GameState]()

  def defaultUpdateState(state: GameState): GameState = state.copy(counter = state.counter + 1)

  def renderState(state: GameState): Unit = IO(println(s"Counter: ${state.counter}"))

  @tailrec
  def engineLoop(state: GameState, lastUpdate: Instant, minInterval: FiniteDuration): Unit =
    val action      = Option(actionQueue.poll()).getOrElse(identity[GameState] _)
    val newState    = action(state)
    val currentTime = Instant.now()
    println(s"Counter: ${newState.counter} wait: ${minInterval.toMillis}")
    val deltaTime     = java.time.Duration.between(lastUpdate, currentTime).toMillis.millis
    val remainingWait = (minInterval - deltaTime).max(Duration.Zero)
    Thread.sleep(minInterval.toMillis)
    engineLoop(newState, currentTime, minInterval)

  new Thread(() => {
    Thread.sleep(3000) // Aspetta 3 secondi
    actionQueue.put(state =>
      println(s"State $state")
      state.copy(counter = state.counter + 10)
    )                  // Aggiunge un'azione
    Thread.sleep(3000) // Aspetta altri 3 secondi
    actionQueue.put(state =>
      println(s"State $state")
      state.copy(counter = state.counter * 2)
    )                  // Aggiunge un'altra azione
    Thread.sleep(3000) // Aspetta altri 3 secondi
  }).start()

  implicit val runtime: IORuntime = IORuntime.global
  val initialState                = GameState(counter = 0)
  val interval                    = 1.second

  incrementWithFuture().onComplete(_ => println("COMPLETE"))(using ExecutionContext.global)

  // Avvia il loop temporizzato
  engineLoop(initialState, Instant.now(), interval)

  def incrementWithFuture(): Future[GameState] =
    val promise = Promise[GameState]()
    actionQueue.offer((state: GameState) => {
      val newState = state.copy(counter = state.counter + 1)
      promise.success(newState)
      newState
    })
    promise.future
