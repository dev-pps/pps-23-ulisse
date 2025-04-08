# Implementazione - Bravetti Federico
Il codice prodotto durante lo svolgimento del progetto riguarda prevalentemente le seguente parti:
- **Station**: Implementazioni delle stazioni con relativo `StationManager` e `StationService`, utilizzando `ValidatedNec` per gestire contemporaneamente tutti gli errori di validazione evitando la dinamica fail-fast
- **Simulation Environment**: implementazione dell'ambiente di simulazione comprese tutte le entità dinamiche costruite estendendo le componenti statiche, in particolare:
  - **Platform & Track**: implementazione delle piattaforme e dei binari, necessari a contenere e gestire i `TrainAgent`.
  - **StationEnvironmentElement & StationEnvironment**: implementazione delle `Station` come elementi dell'ambiente composte da `Platform` e relativo ambiente per la loro gestione.
  - **RouteEnvironmentElement & RouteEnvironment**: implementazione delle `Route` come elementi dell'ambiente composte da `Track` e relativo ambiente per la loro gestione.
  - **DynamicTimetable & DynamicTimetableEnvironment**: implementazione delle `Timetable` come elementi dell'ambiente contenenti le informazioni dinamiche degli orari e relativo ambiente per la loro gestione.
  - **RailwayEnvironment**: implementazione dell'ambiente di simulazione che contiene e coordina `StationEnvironment`, `RouteEnvironment`, `DynamicTimetableEnvironment` e `TrainAgent`.
- **Perception System**: implementazione del sistema di percezione per i `SimulationAgent`.
- **Engine**: implementazione del motore per l'avanzamento della simulazione con relativi `SimulationManager`, `SimulationService`, `SimulationInfoService`, `NotificationService`
- **Statistics**: implementazione di metodi di utilità per il calcolo di statistiche relative al `RailwayEnvironment`.
Inoltre è stata gestita la parte di setup della repo github, e la configurazione delle pipeline di CI/CD per building, testing e delivery.

Di seguito saranno descritte con maggior dettaglio le parti più salienti.

## F-Bounded Polymorphism
Una strategia usata in modo diffuso nell'implementazioni delle classi relative alla simulazione è l'uso del F-Bounded Polymorphism. 
Questa tecnica permette di parametrizzare un tipo dipendentemente da un suo sottotipo e si dimostra particolamente utile nel contesto funzionale, poiché è tipico avere come tipo di ritorno una nuova istanza modificata dell'oggetto piuttosto che effettuare side-effect di modifica. 
Ad esempio, si consideri la definizione del tipo `TrainAgentsContainer`.

Normalmente, si potrebbe definire il tipo `TrainAgentsContainer` come segue:
```scala 3
trait TrainAgentsContainer:
  def updateTrain(train: TrainAgent): TrainAgentsContainer
  def removeTrain(train: TrainAgent): TrainAgentsContainer
``` 
Ma così facendo andando ad invocare la funzione `updateTrain` o `removeTrain` si otterrebbe un nuovo oggetto del tipo base `TrainAgentsContainer` andando a perdere le informazioni sul sottotipo da cui è stata invocata.

La soluzione adottata prevede invece una definizione di questo tipo:
```scala 3
trait TrainAgentsContainer[TAC <: TrainAgentsContainer[TAC]]:
  self: TAC =>
  def updateTrain(train: TrainAgent): Option[TAC]
  def removeTrain(train: TrainAgent): Option[TAC]
```  
In questo modo, il tipo `TAC` è parametrizzato con il proprio sottotipo, permettendo di mantenere le informazioni sul tipo effettivo dell'oggetto e di restituire un nuovo oggetto dello stesso tipo.

Considerazioni su questo costrutto:
- L'utilizzo di un semplice generico non permette di garantire che il tipo `TAC` sia un sottotipo di `TrainAgentsContainer[TAC]`.
- L'utilizzo di del Self-Type permette di vincolare il tipo `TAC` a dover essere mixato con il trait `TrainAgentsContainer[TAC <: TrainAgentsContainer[TAC]]`. 

Così facendo non si permette di creare sotto tipi della forma:
```scala 3
trait Platform extends TrainAgentsContainer[Track]
```  
ma solo sotto tipi della forma 
```scala 3
trait Track extends TrainAgentsContainer[Track]
```  
  
## Perception System
Le percezioni sono fondamentali in un simulatore agent-based, in quanto permettono agli agenti di percepire l'ambiente circostante e di prendere decisioni 
in base a queste informazioni. Per cui in generale le percezioni di un agente dipendono dal tipo dell'agente e dal tipo dell'ambiente dove si trova.

![An image](/resources/implementation/bravetti/PerceptionProvider.svg)

Per fare ciò si è scelto di ultizzare la Type Class `PerceptionProvider` definita nel seguente modo:
```scala 3
trait PerceptionProvider[EC <: EnvironmentsCoordinator[EC], SA <: SimulationAgent[SA]]:
    type P <: Perception[?]
    def perceptionFor(environment: EC, agent: SA): Option[P]
```
In questo modo è possibile definire in maniera mirata solo le percezioni necessarie ad esempio:
```scala 3 
given PerceptionProvider[RailwayEnvironment, TrainAgent] with
    type P = TrainAgentPerception[?]
    def perceptionFor(env: RailwayEnvironment, agent: TrainAgent): Option[P] =
        (agent.findIn(env.stations), agent.findIn(env.routes)) match
        case (Some(_), _)     => Some(TrainPerceptionInStation(trainPerceptionInStation(agent, env)))
        case (_, Some(route)) => Some(TrainPerceptionInRoute(trainPerceptionInRoute(agent, route, env)))
        case _                => None
```
Il `PerceptionProvider` è stato integrato nell'interazione EnvironmentsCoordinator-SimulationAgent nel seguente modo:
```scala 3 
  trait EnvironmentsCoordinator[EC <: EnvironmentsCoordinator[EC]]:
    self: EC =>
    def perceptionFor[SA <: SimulationAgent[SA]](simulationAgent: SA)(using provider: PerceptionProvider[EC, SA]): Option[provider.P] =
      provider.perceptionFor(this, simulationAgent)
```
```scala 3
trait SimulationAgent[SA <: SimulationAgent[SA]]:
    self: SA =>
    type EC <: EnvironmentsCoordinator[EC]
    def doStep(dt: Int, environment: EC): SA =
      /* Then having in context a PerceptionProvider[EC, SA]*/
      environment.perceptionFor(this).map:
        case p: TrainPerceptionInRoute => ...
        case p: TrainPerceptionInStation => ...
```
In particolare si sfrutta nell'environment il Path-Dependent Type `P` per rendere il tipo di ritorno della `perceptionFor` ad-hoc per il tipo di agente che successivamente potrà recuperare la percezione e facendo pattern-matching.

# ComparisonDSL
Lo scopo di questo DSL è quello di fornire un modo agile per confrontare due oggetti di tipo `O` rispetto a una serie di campi `F`, di modo da rendere più compatta la verifica degli aggiornamenti dei diversi oggetti.
La sintassi che si vuole ottenere è del tipo:
```scala 3
val manager, oldManager: SimulationManager
manager.engine compareTo oldManager.engine ignoring State shouldBeBoolean true
```
Per fare questo si è definito il concetto di `Field` e la Type Class `FieldComparator`:
```scala 3
trait Field[F <: Field[F, O], O <: Any]:
  self: F =>
  def values: Seq[F]
```
```scala 3
trait FieldComparator[F <: Field[F, O], O <: Any]:
  def fields: Seq[F]
  final def compare(objects: List[O], ignoredFields: Seq[F]): Boolean =
    val fieldsToCompare = fields.filterNot(ignoredFields.contains)
    objects match
      case firstObject :: tail => tail.forall: otherObject =>
           fieldsToCompare.forall(_compare(firstObject, otherObject, _))
      case _ => false
  protected def _compare(obj: O, otherObj: O, field: F): Boolean
```
Successivamente si è definito il `ComparisonBuilder` che permette di ottenere la struttura sintattica desiderata.

```scala 3
case class ComparisonBuilder[F <: Field[F, O], O <: Any](objects: List[O], ignoredFields: Seq[F])(using fieldComparator: FieldComparator[F, O]):
  def ignoring(field: F, fields: F*): ComparisonBuilder[F, O] =
    copy(ignoredFields = ignoredFields ++ (fields :+ field))

  def considering(field: F, fields: F*): ComparisonBuilder[F, O] =
    copy(ignoredFields = field.values.filterNot((fields :+ field).contains).toIndexedSeq)
  
  def andTo(nextObj: O): ComparisonBuilder[F, O] =
    copy(objects = nextObj +: objects)
    
  def compare: Boolean =
    fieldComparator.compare(objects, ignoredFields)

extension [F <: Field[F, O], O <: Any](obj: O)
  def compareTo(otherObj: O)(using fieldComparator: FieldComparator[F, O]): ComparisonBuilder[F, O] =
    ComparisonBuilder(List(obj, otherObj), Seq[F]())
```

Infine la valutazione finale del confronto avviene tramite la conversione implicita di `ComparisonBuilder` in `Boolean`:
```scala 3
given [T <: Field[T, O], O <: Any]: Conversion[ComparisonBuilder[T, O], Boolean] with
  def apply(builder: ComparisonBuilder[T, O]): Boolean =
    builder.compare
```

Dato che la funzione base `shouldBe` per valutare le asserzioni è generica non viene effettuata la conversione implicita. 
Per questo è stato necessario introdurre un metodo `shouldBeBoolean` per forzarne la valutazione
```scala 3
extension (b1: Boolean)
  def shouldBeBoolean(b2: Boolean): Unit =
    b1 shouldBe b2
```

Infine, per utilizzare il DSL è necessario definire un `Field` specifico per il tipo di oggetto che si vuole confrontare e un `FieldComparator` che definisca come effettuare il confronto.
```scala 3
enum EngineField extends Field[EngineField, Engine]:
  case Running, Configuration, State
  def values: Seq[EngineField] = EngineField.values.toSeq      
```
```scala 3
given FieldComparator[EngineField, Engine] with
  def fields: Seq[EngineField] = EngineField.values.toSeq
  def _compare(firstEngine: Engine, otherEngine: Engine, field: EngineField): Boolean =
    field match
      case EngineField.Running       => firstEngine.running == otherEngine.running
      case EngineField.Configuration => firstEngine.configuration == otherEngine.configuration
      case EngineField.State         => firstEngine.state == otherEngine.state
```
## CollectionUtils e OptionUtils
Per le dinamiche di aggiornamento dei `TrainAgent` nei diversi  `EnvironmentElement` e `TrainAgentContainer` è risultato spesso necessario dover effettuare operazioni di ricerca e modifica(e.g. ricerca della track/platform libera, ricerca del treno da sostituire).
Inoltre queste modifiche dovevano essere effettuate solo al verificarsi di determinate condizioni.
Per rendere queste operazioni più compatte e leggibili sono stati introdotti dei metodi di utilità per le `Collection` e `Option`.
Considerando l'esempio seguente
```scala 3
private final case class RouteEnvironmentElementImpl(route: Route, containers: Seq[Track])
        extends RouteEnvironmentElement:
  export route.*
  override def constructor(containers: Seq[Track]): RouteEnvironmentElement =
    copy(containers = containers)

  override def putTrain(train: TrainAgent, direction: TrackDirection): Option[RouteEnvironmentElement] =
    (for
      firstAvailableContainer <- containers.find(_.isAvailable(direction))
      updatedContainers <- containers.updateWhenWithEffects(_ == firstAvailableContainer)(_.putTrain(train, direction))
    yield constructor(updatedContainers)) when !contains(train) && isAvailableFor(train, direction)
```    
si nota l'utilizzo delle funzioni:
- `updateWhenWithEffects`: per inserire il treno nella prima rotaia disponibile  
- `when`; per valutare la condizione di inserimento del treno prima di procedere con l'aggiornamento effettivo.

### CollectionUtils
Riguardo le `Collection` è stato definito oltre al metodo `updateWhenWithEffects` anche il metodo `updateWhen` da utilizzare nel caso in cui l'aggiornamento 
ritorni direttamente l'oggetto aggionato senza incapsularlo in un Higher-Order Type. Per implementare le due funzioni rispettando il principio `DRY` 
è stato definito un metodo `wrappedUpdate` che tramite l'utilizzo del Type Alias `Id` incapsula la funzione di aggiornamento adattandola per l'utilizzo nel metodo `updateWhenWithEffects`.
```scala 3
object CollectionUtils:
    private def wrappedUpdate[A](update: A => A)(in: A): Id[A] = Id(update(in))
    
    extension [F[_]: Traverse, A](collection: F[A])
      def updateWhen(condition: A => Boolean)(update: A => A): F[A] =
        collection.updateWhenWithEffects(condition)(wrappedUpdate(update))
      
      def updateWhenWithEffects[W[_]: Monad](condition: A => Boolean)(update: A => W[A]): W[F[A]] =
        collection.traverse(item => if condition(item) then update(item) else item.pure[W])

```
### OptionUtils
Per quanto riguarda gli `Option` è stato definito il metodo `when` come wrap del metodo `Option.when` per renderne l'uso più fluente, si noti l'utilizzo del parametro `by-name` così da valutare il risultato solo nel momento in cui la condizione risulta verificata.
Una nota di dettaglio riguarda la definizione della conversione implicita da `Option[Option[A]]` a `Option[A]` che è risultata utile nel caso in cui l'`optionalResult` sia esso stesso un `Option[A]`.
```scala 3
object OptionUtils:
    extension [A](optionalResult: => A)
      def when(condition: Boolean): Option[A] =
        Option.when(condition)(optionalResult)
    
    given [A]: Conversion[Option[Option[A]], Option[A]] = _.flatten
```

## Time Operation
Per gestire la simulazione è stato necessario introdurre un tipo `Time` essendo questo strettamente legato alla gestione delle `Timetable`. Di conseguenza è stato necessario definire le operazioni di somma e differenza per calcolare:
- Aggiornamento del tempo dell'ambiente
- Calcolo degli orari effettivi
- Calcolo dei ritardi

In particolare, per quanto riguarda l'aggiornamento del tempo dell'ambiente si vuole trattare il tempo in modo ciclico, mentre negli altri due casi
si vuole tenere conto di eventuali overflow/underflow e accumulare i valori in eccesso nel campo delle ore.

Per ottenere un risultato `DRY` è stato utilizzato il pattern `Strategy` in combinazione con i parametri contestuali per determinare il costruttore da utilizzare.

>NOTA: Oltre a gestire le diverse stategie di somma si è dovuta gestire anche la possibilità che i tempi siano wrappati in tipi Higher-Order come Option o Either.

Di seguito sono riportate le implementazioni delle operazioni di somma.
```scala 3
extension [M[_]: Monad, T <: Time](time1: M[T])
  def +(time2: M[T])(using constructor: TimeConstructor[M[T]]): M[T] =
    extractAndPerform(time1, time2): (t1, t2) =>
      val secondsInADay = Time.secondsInMinute * Time.minutesInHour * Time.hoursInDay
      buildOverflowTimeFromSeconds(adaptTimeUnitToBound(t1.toSeconds + t2.toSeconds, secondsInADay))

  def overflowSum(time2: M[T])(using constructor: TimeConstructor[M[T]]): M[T] =
    extractAndPerform(time1, time2): (t1, t2) =>
      buildOverflowTimeFromSeconds(t1.toSeconds + t2.toSeconds)
```

Dovendo mantenere la coerenza con il tipo di ritorno è stato definita la Type Class `TimeConstructor` così da fornire il costruttore specifico per il tempo a seconda della situazione.
```scala 3
sealed trait TimeConstructor[T]:
  def construct(h: Int, m: Int, s: Int): T
```
Ad esempio è stato poi definito il costruttore di default per il tipo `Time`.
```scala 3
given TimeConstructor[Time] with
  def construct(h: Int, m: Int, s: Int): Time = Time(h, m, s)
```

Inoltre essendo in generale i tempi incapsulati prima è necessario spacchettarli, per questo è stata definita la funzione `extractAndPerform`.
```scala 3
private def extractAndPerform[M[_]: Monad, T <: Time, R](t1: M[T],t2: M[T])(f: (T, T) => M[R]): M[R] =
    for
        time1 <- t1
        time2 <- t2
        result   <- f(time1, time2)
    yield result
```

La funzione `adaptTimeUnitToBound` esegue una classica operazione di normalizzazione che garantisce che il valore `timeUnit` appartenga all'intervallo `[0, timeBound-1]`, nello specifico in corrispondenza del rappresentante della classe di resto `mod timeBound`.
```scala 3
  private def adaptTimeUnitToBound(timeUnit: Int, timeBound: Int): Int =
    ((timeUnit % timeBound) + timeBound) % timeBound
```
Così facendo si garantisce che i secondi complessivi risultanti non saranno soggetti ad overflow al momento della conversione in `Time` e l'orario risultante sarà coerente sia in presenza di secondi positivi che negativi.

Infine la funzione `buildOverflowTimeFromSeconds`, riportata di seguito, permette di costruire un nuovo oggetto `Time` a partire dal numero di secondi (adattandoli al formato `h:m:s`) ed accumulando eventuali eccessi nel campo delle ore.
```scala 3
  private def buildTimeFromSeconds[M[_]: Monad, T <: Time](seconds: Int)
    (using constructor: TimeConstructor[M[T]]): M[T] =
    constructor.construct.tupled(
      seconds / (Time.secondsInMinute * Time.minutesInHour),
      seconds / Time.secondsInMinute % Time.minutesInHour,
      seconds % Time.secondsInMinute
    )
```

## Runner for Test
L'utilizzo di una LazyList collegata ad una ConcurrentQueue permette di rappresentare il cambiamento dello stato come una sequenza di trasformazioni di quest'ultimo. 
In questo modo si ha una migliore integrazione con lo stile funzionale e inoltre risulta più semplice poter navigare tra gli stati passati semplificando le operazioni di debug, caratteristica che non si sarebbe pouta ottenere andando ad utilizzare una semplice refernza concorrente.
```scala 3
def runAll[S](initialState: S, queue: LinkedBlockingQueue[S => S]): List[S] =
val elements = java.util.ArrayList[S => S]()
queue.drainTo(elements)
elements.asScala.toList.scanLeft(initialState)((state, event) => event(state))
```
```scala 3
"runAll" should:
"correctly apply all transformations from the queue to the initial state" in:
val queue = LinkedBlockingQueue[String => String]()
queue.add(_ + "A")
queue.add(_ + "B")
queue.add(_ + "C")
runAll("", queue) shouldEqual List("", "A", "AB", "ABC")
```
## Mocks In Testing
Uno dei vantaggi dell'architettura esagonale è quello di favorire l'utilizzo di mock come test doubles, 
per cui nello sviluppo di alcuni test si è fatto uso di questi strumenti per simulare il comportamento di alcune parti del sistema.

L'efficacia dei mock si nota principalmente nello sviluppo dei test che coinvolgono le porte del sistema ad esempio:
### SimulationInfoAdapter
In questo caso è possibile effettuare dei test all'esterno del sistema simulando il comportamento della porta `SimulationInfoPorts.Input` e verificare il corretto funzionamento dell'adapter senza dover gestire l'evoluzione dello stato interagendo con la queue degli eventi.
```scala 3
private val mockedPort            = mock[SimulationInfoPorts.Input]
private val simulationInfoAdapter = SimulationInfoAdapter(mockedPort)

"SimulationInfoAdapter" when:
  "query for station info" should:
    val station                   = mock[Station]
    val stationEnvironmentElement = mock[StationEnvironmentElement]
    "return the station info if present" in:
      when(mockedPort.stationInfo(station)).thenReturn(Future.successful(Some(stationEnvironmentElement)))
      Await.result(simulationInfoAdapter.stationInfo(station), Duration.Inf) shouldBe Some(stationEnvironmentElement)

    "return none if the station info is not present" in:
      when(mockedPort.stationInfo(station)).thenReturn(Future.successful(None))
      Await.result(simulationInfoAdapter.stationInfo(station), Duration.Inf) shouldBe None
```
### SimulationManager
In questo caso si può verificare come la scelta di definire un provider esterno per il tempo ha permesso poi di utilizzare un comportamento mocked 
per ottenere uno scorrimento deterministico del tempo e effettuare al meglio il test del motore di simulazione.
```scala 3
private val timeProvider  = mock[UtilityPorts.Output.TimeProviderPort]
private val startTime     = 10L
private val timeIncrement = 5L
private def setupTimeProvider(): Unit =
  val timeIterator = LazyList.iterate(startTime)(_ + timeIncrement).iterator
  when(timeProvider.currentTimeMillis()).thenAnswer((_: InvocationOnMock) => timeIterator.next())
```
