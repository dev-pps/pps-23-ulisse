# Implementazione Federico Bravetti
Il codice prodotto durante lo svolgimento del progetto riguarda prevalentemente le seguente parti:
- **Station**: Implementazioni delle stazioni con relativo manager e servizio
- **Simulation Environment**: implementazione dell'ambiente di simulazione comprese tutte le entità dinamiche costruite estendendo le componenti statiche, in particolare:
  - **Platform & Track**: implementazione delle piattaforme e dei binari, necessari a contenere e gestire i `TrainAgent`.
  - **StationEnvironmentElement & StationEnvironment**: implementazione di un elemento di ambiente per le stazioni.
  - **RouteEnvironmentElement & RouteEnvironment**: implementazione di un elemento di ambiente per le rotte.
  - **DynamicTimetable & DynamicTimetableEnvironment**: implementazione di un orario dinamico per le stazioni.
  - **RailwayEnvironment**: implementazione dell'ambiente di simulazione.
- **PerceptionSystem**: implementazione di un sistema di percezione per gli agenti di simulazione.
- **Engine**: implementazione di un motore di simulazione.

```scala 3
trait TrainAgentsContainer[TAC <: TrainAgentsContainer[TAC]]:
  self: TAC =>
  def updateTrain(train: TrainAgent): Option[TAC]
  def removeTrain(train: TrainAgent): Option[TAC]
```  

```scala 3
trait TrainAgentsContainer[TAC]
```  
```scala 3
trait TrainAgentsContainer[TAC <: TrainAgentsContainer[TAC]]:
```
```scala 3
trait TrainAgentsContainer[TAC <: TrainAgentsContainer[TAC]]:
  self: TAC =>
```  
PerceptionSystem
![An image](/resources/implementation/bravetti/PerceptionProvider.svg)
```scala 3
trait PerceptionProvider[EC <: EnvironmentsCoordinator[EC], SA <: SimulationAgent[SA]]:
    type P <: Perception[?]
    def perceptionFor(environment: EC, agent: SA): Option[P]
```

```scala 3 
  trait EnvironmentsCoordinator[EC <: EnvironmentsCoordinator[EC]]:
    self: EC =>
    def perceptionFor[SA <: SimulationAgent[SA]](simulationAgent: SA)(using
        provider: PerceptionProvider[EC, SA]
    ): Option[provider.P] =
      provider.perceptionFor(this, simulationAgent)
```
```scala 3 
given PerceptionProvider[RailwayEnvironment, TrainAgent] with
    type P = TrainAgentPerception[?]
    def perceptionFor(env: RailwayEnvironment, agent: TrainAgent): Option[P] =
        (agent.findIn(env.stations), agent.findIn(env.routes)) match
        case (Some(_), _)     => Some(TrainPerceptionInStation(trainPerceptionInStation(agent, env)))
        case (_, Some(route)) => Some(TrainPerceptionInRoute(trainPerceptionInRoute(agent, route, env)))
        case _                => None
```
```scala 3
trait SimulationAgent[SA <: SimulationAgent[SA]]:
  self: SA =>
  type EC <: EnvironmentsCoordinator[EC]
  def doStep(dt: Int, environment: EC): SA =
    /* Then having in context a PerceptionProvider[EC, SA]*/
    val perception = environment.perceptionFor(this)
```

```scala 3
trait TrainAgentEnvironment[TAE <: TrainAgentEnvironment[TAE, EE], EE <: TrainAgentEEWrapper[EE]]:
    self: TAE =>

trait TrainAgentEnvironment[TAE <: TrainAgentEnvironment[TAE]]:
  self: TAE =>
  type EE <: TrainAgentEEWrapper[?] //No information about the type of the EE
```
    
```scala 3
def removeTrain(train: TrainAgent): Option[TAE] = doOperationOn(train, _.removeTrain(train))
protected def constructor(environmentElements: Seq[EE]): TAE
private def doOperationOn(train: TrainAgent, operation: EE => Option[EE]): Option[TAE] =
      for
        ee        <- environmentElements.find(_.contains(train))
        updatedEE <- operation(ee) /* operation is like an update or remove */
      yield constructor(environmentElements.swapWhenEq(ee)(updatedEE))
```

```scala 3
trait Field[T <: Field[T, O], O <: Any]:
  self: T =>
  def values: Seq[T]


trait FieldComparator[T <: Field[T, O], O <: Any]:
  def fields: Seq[T]
  final def compare(objects: List[O], ignoredFields: Seq[T]): Boolean =
    val fieldsToCompare = fields.filterNot(ignoredFields.contains)
    objects match
      case firstObject :: tail => tail.forall: otherObject =>
           fieldsToCompare.forall(_compare(firstObject, otherObject, _))
      case _ => false

  protected def _compare(obj: O, otherObj: O, field: T): Boolean
    
given [T <: Field[T, O], O <: Any]: Conversion[ComparisonBuilder[T, O], Boolean] with
  def apply(builder: ComparisonBuilder[T, O]): Boolean =
    builder.compare


case class ComparisonBuilder[T <: Field[T, O], O <: Any](objects: List[O], ignoredFields: Seq[T])(using fieldComparator: FieldComparator[T, O]):
    def ignoring(field: T, fields: T*): ComparisonBuilder[T, O] =
      copy(ignoredFields = ignoredFields ++ (fields :+ field))

    def considering(field: T, fields: T*): ComparisonBuilder[T, O] =
      copy(ignoredFields = field.values.filterNot((fields :+ field).contains).toIndexedSeq)

    def andTo(nextObj: O): ComparisonBuilder[T, O] =
      copy(objects = nextObj +: objects)

    def compare: Boolean =
      fieldComparator.compare(objects, ignoredFields)

extension [T <: Field[T, O], O <: Any](obj: O)
    def compareTo(otherObj: O)(using fieldComparator: FieldComparator[T, O]): ComparisonBuilder[T, O] =
    ComparisonBuilder(List(obj, otherObj), Seq[T]()

  val manager, oldManager: SimulationManager
  manager.engine compareTo oldManager.engine ignoring State shouldBeBoolean true

    extension (b1: Boolean)
      def shouldBeBoolean(b2: Boolean): Unit =
        b1 shouldBe b2

    given FieldComparator[EngineField, Engine] with
      def fields: Seq[EngineField] = EngineField.values.toSeq
      def _compare(firstEngine: Engine, otherEngine: Engine, field: EngineField): Boolean =
        field match
          case EngineField.Running       => firstEngine.running == otherEngine.running
          case EngineField.Configuration => firstEngine.configuration == otherEngine.configuration
          case EngineField.State         => firstEngine.state == otherEngine.state
    
    enum EngineField extends Field[EngineField, Engine]:
      case Running, Configuration, State
      def values: Seq[EngineField] = EngineField.values.toSeq      
```

[//]: # ()
[//]: # (//  case class EE&#40;environmentElements: Seq[RouteEnvironmentElement]&#41; extends TrainAgentEnvironment2[EE]:)

[//]: # (//      override type AA = TrainAgentEEWrapper[RouteEnvironmentElement])

[//]: # (//    override protected def constructor&#40;environmentElements: Seq[RouteEnvironmentElement]&#41;: EE =)

[//]: # (//      copy&#40;environmentElements&#41;)

[//]: # (//)

[//]: # (//  trait TrainAgentEnvironment2[TAE <: TrainAgentEnvironment2[TAE]]:)

[//]: # (//    self: TAE =>)

[//]: # (//    type AA <: TrainAgentEEWrapper[AA])

[//]: # (//    def environmentElements: Seq[AA])

[//]: # (//    protected def constructor&#40;environmentElements: Seq[AA]&#41;: TAE)

[//]: # (////    def updateTrain&#40;train: TrainAgent&#41;: Option[TAE] = doOperationOn&#40;train, _.updateTrain&#40;train&#41;&#41;)

[//]: # (//    def removeTrain&#40;train: TrainAgent&#41;: Option[TAE] = doOperationOn&#40;train, _.removeTrain&#40;train&#41;&#41;)

[//]: # (//    private def doOperationOn&#40;)

[//]: # (//                               train: TrainAgent,)

[//]: # (//                               operation: AA => Option[AA])

[//]: # (//                             &#41;: Option[TAE] =)

[//]: # (//      for)

[//]: # (//        ee <- environmentElements.find&#40;_.contains&#40;train&#41;&#41;)

[//]: # (//        updatedEE <- operation&#40;ee&#41;)

[//]: # (//      yield constructor&#40;environmentElements.swapWhenEq&#40;ee&#41;&#40;updatedEE&#41;&#41;)

[//]: # ()
[//]: # ()


```scala 3
object CollectionUtils:
    private def wrappedUpdate[A](update: A => A)(in: A): Id[A] = Id(update(in))
    
    extension [F[_]: Traverse, A](collection: F[A])
    /** Update all elements in the collection that satisfy the condition. */
    def updateWhen(condition: A => Boolean)(update: A => A): F[A] =
    collection.updateWhenWithEffects(condition)(wrappedUpdate(update))
    
        /** Swap all elements in the collection that satisfy the condition. */
        def swapWhen(condition: A => Boolean)(element: A): F[A] =
          collection.updateWhen(condition)(_ => element)
    
        /** Update all elements in the collection that are equal to the given element. */
        def swapWhenEq(find: A)(replace: A): F[A] =
          collection.updateWhen(_ == find)(_ => replace)
    
        /** Update all elements in the collection that satisfy the condition, handling effects. */
        def updateWhenWithEffects[W[_]: Monad](condition: A => Boolean)(update: A => W[A]): W[F[A]] =
          collection.traverse(item => if condition(item) then update(item) else item.pure[W])
    
        /** Swap all elements in the collection that satisfy the condition, handling effects. */
        def swapWhenWithEffects[W[_]: Monad](condition: A => Boolean)(element: W[A]): W[F[A]] =
          collection.updateWhenWithEffects(condition)(_ => element)
    
        /** Swap all elements in the collection that are equal to the given element, handling effects. */
        def swapWhenEqWithEffects[W[_]: Monad](find: A)(replace: W[A]): W[F[A]] =
          collection.updateWhenWithEffects(_ == find)(_ => replace)

object OptionUtils:
    extension [A](optionalResult: => A)
      /** Compute and returns an `Option` containing the result if the condition is `true`, otherwise `None`. */
      def when(condition: Boolean): Option[A] =
        Option.when(condition)(optionalResult)
    
    /** Defines the conversion from flatten `Option[Option[A]]` to `Option[A]`. */
    given [A]: Conversion[Option[Option[A]], Option[A]] = _.flatten

  private final case class RouteEnvironmentElementImpl(route: Route, containers: Seq[Track])
    extends RouteEnvironmentElement:
  export route.*
  override def constructor(containers: Seq[Track]): RouteEnvironmentElement =
    copy(containers = containers)

  override def putTrain(train: TrainAgent, direction: TrackDirection): Option[RouteEnvironmentElement] =
    (for
      firstAvailableContainer <- containers.find(_.isAvailable(direction))
      updatedContainers <-
        containers.updateWhenWithEffects(_ == firstAvailableContainer)(_.putTrain(train, direction))
    yield constructor(updatedContainers)) when !contains(train) && isAvailableFor(train, direction)
```

```scala 3
private def extractAndPerform[M[_]: Monad, T <: Time, R](t1: M[T],t2: M[T])(f: (T, T) => M[R]): M[R] =
    for
        time1 <- t1
        time2 <- t2
        res   <- f(time1, time2)
    yield res

trait TimeConstructor[T]:
  def construct(h: Int, m: Int, s: Int): T

given TimeConstructor[Time] with
  def construct(h: Int, m: Int, s: Int): Time = Time(h, m, s)


extension [M[_]: Monad, T <: Time](time: M[T])
  /** Adds two times */
  @targetName("add")
  def +(time2: M[T])(using constructor: TimeConstructor[M[T]]): M[T] =
    extractAndPerform(time, time2): (t, t2) =>
      calculateSum(t, t2)

  /** Adds two times with overflow */
  def overflowSum(time2: M[T])(using constructor: TimeConstructor[M[T]]): M[T] =
    extractAndPerform(time, time2): (t, t2) =>
      calculateOverflowSum(t, t2)

private trait TimeBuildStrategy:
  def buildTimeValue(h: Int, m: Int, s: Int): (Int, Int, Int)

private given defaultStrategy: TimeBuildStrategy with
  def buildTimeValue(h: Int, m: Int, s: Int): (Int, Int, Int) =
    (
      ((h % Time.hoursInDay) + Time.hoursInDay)           % Time.hoursInDay,
      ((m % Time.minutesInHour) + Time.minutesInHour)     % Time.minutesInHour,
      ((s % Time.secondsInMinute) + Time.secondsInMinute) % Time.secondsInMinute
    )

private given overflowStrategy: TimeBuildStrategy with
def buildTimeValue(h: Int, m: Int, s: Int): (Int, Int, Int) = (h, m % Time.minutesInHour, s % Time.secondsInMinute)

private def calculateSum[M[_]: Monad, T <: Time](time1: T, time2: T)(
  using constructor: TimeConstructor[M[T]]): M[T] =
    given TimeBuildStrategy = defaultStrategy
    buildTimeFromSeconds(time1.toSeconds + time2.toSeconds)

  private def calculateOverflowSum[M[_]: Monad, T <: Time](time1: T, time2: T)(using
                                                                               constructor: TimeConstructor[M[T]]
  ): M[T] =
    given TimeBuildStrategy = overflowStrategy
    buildTimeFromSeconds(time1.toSeconds + time2.toSeconds)

  private def buildTimeFromSeconds[M[_]: Monad, T <: Time](seconds: Int)(using
                                                                         constructor: TimeConstructor[M[T]]
  )(using buildStrategy: TimeBuildStrategy): M[T] =
    constructor.construct.tupled(buildStrategy.buildTimeValue(
      seconds / (Time.secondsInMinute * Time.minutesInHour),
      seconds / Time.secondsInMinute,
      seconds
    ))
```

