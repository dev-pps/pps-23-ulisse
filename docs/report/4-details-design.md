[//]: # (![An image]&#40;/resources/architecture/staticEntitiesManagers.svg&#41;)

[//]: # (![An image]&#40;/resources/architecture/queuingsystem.svg&#41;)

[//]: # (![An image]&#40;/resources/architecture/EnvironmentElementsArchitecture.svg&#41;)

[//]: # (![An image]&#40;/resources/architecture/EnvironmentArchitecture.svg&#41;)

[//]: # (![An image]&#40;/resources/architecture/RailwayEnvironment.svg&#41;)

[//]: # (![An image]&#40;/resources/architecture/SimulationEngineWithManager.svg&#41;)

# Organizzazione del codice

![An image](/resources/architecture/packages.svg)

Il codice è organizzato in 6 package principali:

- entities: contiene le classi che rappresentano le entità del dominio, modellando gli oggetti principali.
- applications: include le classi che implementano la logica di business:
    - managers: gestiscono e coordinano le entità in base ai requisiti applicativi.
    - ports: definiscono le interfacce attraverso cui la logica di business comunica con l'esterno.
    - useCases: implementano le porte di input, offrendo i servizi esposti dall'applicazione.
    - events: definiscono le regole e le operazioni per l'aggiornamento dello stato dell'applicazione.
- adapters: contiene i componenti che fungono da interfacce tra la logica di business e i sistemi esterni, facilitando
  la comunicazione tra il core e i componenti periferici.
- infrastructures: racchiude le classi responsabili della gestione della visualizzazione grafica e della definizione di
  tempo.
- DSL: include le classi che forniscono un linguaggio specifico per la creazione delle entità dell'applicazione e per la
  definizione dell'intera railway, semplificando la configurazione del dominio applicativo.
- utils: contiene le classi di utilità che offrono funzioni di supporto, astraggendo operazioni comuni necessarie per il
  funzionamento del sistema.

## Architettura

### AppState

All’interno dell’_Application_, la gestione dello stato è delegata ai
`Managers`, ognuno responsabile di un aggregato specifico di entità di dominio.
Ogni manager implementa le operazioni di persistenza, aggiornamento e
cancellazione, assicurando l’integrità dei dati e la gestione degli errori
secondo le regole e le invarianti del dominio applicativo.

```mermaid
classDiagram
    direction BT
    class AppState

    class StationManager
    class RouteManager
    class TrainManager
    class TimetableManager
    class SimulationManager

    class Station
    class Route
    class Train
    class Timetable
    class SimulationData
    class Engine

    <<Trait>> Station
    <<Trait>> Route
    <<Trait>> Train
    <<Trait>> Timetable
    <<Trait>> SimulationData
    <<Trait>> Engine

    <<Trait>> AppState
    <<Trait>> StationManager
    <<Trait>> RouteManager
    <<Trait>> TrainManager
    <<Trait>> TimetableManager
    <<Trait>> SimulationManager
    <<Trait>> StationManager
    <<Trait>> RouteManager
    <<Trait>> TrainManager
    <<Trait>> TimetableManager
    <<Trait>> SimulationManager
    Station <--o StationManager: 0..*
    Route <--o RouteManager: 0..*
    Train <--o TrainManager: 0..*
    Timetable <--o TimetableManager: 0..*
    SimulationData <--o SimulationManager: 1
    Engine <--o SimulationManager: 1
    StationManager <--* AppState: 1
    RouteManager <--* AppState: 1
    TrainManager <--* AppState: 1
    TimetableManager <--* AppState: 1
    SimulationManager <--* AppState: 1
```

### EventQueue

La gestione dello stato dell'applicazione è stata implementata a livello _Applicativo_
dell'architettura esagonale per garantire l'immutabilità dell'`AppState` all'interno del
core domain, rispondendo a un requisito di progetto. Questo approccio evita che la
gestione dell'evoluzione dello stato sia delegata a componenti esterni. In questo
contesto, l'`EventQueue` opera in modo simile a un event loop, centralizzando il sistema
degli aggiornamenti e mantenendo la coerenza dello stato.

L’`EventQueue` si occupa della gestione dello stato dell’`AppState`, esponendo, in conformità
ai principi di _SRP_ e _ISP_, interfacce contestuali che delimitano le operazioni disponibili
per ciascun ambito di dominio. Ogni vista specializzata sull’EventQueue consente
l’esecuzione esclusiva delle azioni pertinenti a un particolare aggregato, garantendo
un accesso controllato e una chiara separazione delle responsabilità nella modifica
dello stato applicativo.

```mermaid
classDiagram
    direction TB
    class AppState
    class EventQueue
    class StationEventQueue
    class RouteEventQueue
    class TrainEventQueue
    class TimetableEventQueue
    class SimulationEventQueue
    class TrainEventQueue

    <<Trait>> AppState
    <<Trait>> EventQueue
    <<Trait>> StationEventQueue
    <<Trait>> RouteEventQueue
    <<Trait>> TrainEventQueue
    <<Trait>> TimetableEventQueue
    <<Trait>> SimulationEventQueue
    EventQueue: LinkedBlockingQueue[AppState => AppState] events
    EventQueue: startProcessing(initState AppState) Unit
    StationEventQueue: addReadStationManagerEvent() Unit
    StationEventQueue: addUpdateStationManagerEvent() Unit
    StationEventQueue: addUpdateStationManagersEvent() Unit
    RouteEventQueue: addReadRouteEvent() Unit
    RouteEventQueue: addCreateRouteEvent() Unit
    RouteEventQueue: addUpdateRouteEvent() Unit
    RouteEventQueue: addDeleteRouteEvent() Unit
    TrainEventQueue: addReadTrainEvent() Unit
    TrainEventQueue: addCreateTrainEvent() Unit
    TrainEventQueue: addUpdateTrainEvent() Unit
    TrainEventQueue: addDeleteTrainEvent() Unit
    SimulationEventQueue: addReadSimulationEnvironmentEvent() Unit
    SimulationEventQueue: addSetupSimulationManagerEvent() Unit
    SimulationEventQueue: addUpdateSimulationManagerEvent() Unit
    SimulationEventQueue: addSetupAppStateEvent() Unit
    TimetableEventQueue: addReadTimetableEvent() Unit
    TimetableEventQueue: addCreateTimetableEvent() Unit
    TimetableEventQueue: addUpdateTimetableEvent() Unit
    TimetableEventQueue: addDeleteTimetableEvent() Unit
    AppState <--o EventQueue: 1
    EventQueue --> StationEventQueue
    EventQueue --> RouteEventQueue
    EventQueue --> TrainEventQueue
    EventQueue --> TimetableEventQueue
    EventQueue --> SimulationEventQueue
```

### InputPorts

L’_application_ espone le `InputPorts`, implementate dai rispettivi `Services`,
che definiscono gli `UseCase` dell’applicazione. Gli `Adapters` invocano queste
porte per richiedere operazioni sul dominio, attivando i servizi che, a loro
volta, pubblicano le operazioni sulla `EventQueue` dedicata al contesto di esecuzione.
Questo meccanismo assicura una gestione coordinata e consistente dello stato,
mantenendo il core domain isolato dalle dipendenze esterne.

```mermaid
classDiagram
    direction TB
    class StationService
    class RouteService
    class TrainServices
    class TimetableServices
    class SimulationInfoServices
    class SimulationServices

    class StationPortsInput
    class RoutePortsInput
    class TrainPortsInput
    class TimetablePortsInput
    class SimulationInfoPortsInput
    class SimulationPortsInput

    class StationEventQueue
    class RouteEventQueue
    class TrainEventQueue
    class TimetableEventQueue
    class TrainEventQueue
    class SimulationEventQueue

    <<Trait>> StationEventQueue
    <<Trait>> RouteEventQueue
    <<Trait>> TrainEventQueue
    <<Trait>> TimetableEventQueue
    <<Trait>> SimulationEventQueue
    <<Trait>> StationPortsInput
    <<Trait>> RoutePortsInput
    <<Trait>> TrainPortsInput
    <<Trait>> TimetablePortsInput
    <<Trait>> SimulationPortsInput
    <<Trait>> SimulationInfoPortsInput
    StationPortsInput: stationMap() Future
    StationPortsInput: addStation() Future
    StationPortsInput: removeStation() Future
    StationPortsInput: updateStation() Future
    RoutePortsInput: routes() Future
    RoutePortsInput: save() Future
    RoutePortsInput: modify() Future
    RoutePortsInput: delete() Future
    TrainPortsInput: rains() Future
    TrainPortsInput: createTrain() Future
    TrainPortsInput: removeTrain() Future
    TrainPortsInput: updateTrain() Future
    TimetablePortsInput: createTimetable() Future
    TimetablePortsInput: deleteTimetable() Future
    TimetablePortsInput: timetablesOf() Future
    SimulationInfoPortsInput: stationInfo() Future
    SimulationInfoPortsInput: routeInfo() Future
    SimulationInfoPortsInput: trainInfo() Future
    SimulationPortsInput: initSimulation() Future
    SimulationPortsInput: initSimulationUsing() Future
    SimulationPortsInput: setupEngine() Future
    SimulationPortsInput: start() Future
    SimulationPortsInput: stop() Future
    SimulationPortsInput: reset() Future
    StationEventQueue <--* StationService: 1
    RouteEventQueue <--* RouteService: 1
    TrainEventQueue <--* TrainServices: 1
    TimetableEventQueue <--* TimetableServices: 1
    SimulationEventQueue <--* SimulationServices: 1
    SimulationEventQueue <--* SimulationInfoServices: 1
    StationPortsInput <|-- StationService
    RoutePortsInput <|-- RouteService
    TrainPortsInput <|-- TrainServices
    TimetablePortsInput <|-- TimetableServices
    SimulationPortsInput <|-- SimulationServices
    SimulationInfoPortsInput <|-- SimulationInfoServices 
```

Come conseguenza della gestione immutabile e centralizzata dell’`AppState` nel livello
_Application_, le implementazioni delle `InputPorts` espongono operazioni che restituiscono
una `Future`. Questo modello consente di elaborare le richieste esterne in modo asincrono,
garantendo che l'aggiornamento dello stato avvenga in maniera controllata e non bloccante,
preservando la coerenza e l'integrità del dominio applicativo.

## Railway editor

### Entities

Di seguito è illustrato il design delle entità di dominio statiche, ovvero quelle
entità che vengono definite e create dall'utente durante la fase di editing, prima
dell'esecuzione della simulazione.

#### Station

Di seguito è riportato il modello concettuale della stazione nella fase di definizione,
identificata da un identificativo univoco `id` e composta dagli attributi specificati
nel diagramma. Sono inoltre definiti due possibili errori di creazione, modellati
tramite il tipo `Errors`, a garanzia della validazione dei dati e della coerenza del dominio.

```mermaid
classDiagram
    direction TB
    class Station
    class StationImpl
    class Error

    <<enumeration>> Error
    <<Trait>> Station
    Station: id() Int
    Station: name() String
    Station: coordinate() Coordinate
    Station: withCoordinate(newCoordinate Coordinate) Station
    Station: numberOfPlatforms() Int
    Error: InvalidName
    Error: InvalidNumberOfPlatforms
    Station <|-- StationImpl
    Station .. Error: use
```

#### Route

Le `Route` costituiscono un'entità chiave del dominio e sono identificate da un `IdRoute`,
determinato in funzione della stazione di partenza, della stazione di arrivo e della
tipologia di percorso. Il modello supporta due categorie principali di route:
`Normale` e `AV` (Alta Velocità).
La definizione di una Route comporta l'associazione a un insieme di Track omogenee
rispetto alla tipologia selezionata. Il processo di creazione è vincolato da regole
di integrità, con la gestione degli errori formalizzata attraverso l'enumerazione
`Errors`, che consente di intercettare e rappresentare eventuali anomalie durante la
fase di validazione.

```mermaid
classDiagram
    direction LR
    class Route
    class RouteImpl
    class Technology
    class RouteType
    class Errors

    <<enumeration>> RouteType
    <<enumeration>> Errors
    <<Trait>> Route
    RouteType: Normal RouteType
    RouteType: AV RouteType
    Errors: SameStation Errors
    Errors: FewRails Errors
    Errors: TooManyRails Errors
    Errors: TooShort Errors
    Route: id IdRoute
    Route: departure Station
    Route: arrival Station
    Route: typology RouteType
    Route: railsCount Int
    Route: length Double
    Route <|-- RouteImpl
    Route *--> RouteType: 1
    RouteType *--> Technology: 1
    Route .. Errors: use

```

#### Train

Il `Train` viene identificato dal suo _name_ e si caratterizza per la sua tecnologia 
`TrainTechnology` che gli conferisce specifiche qualità di movimento come l'accelerazione 
di partenza/arresto e la massima velocità che poi verranno utilizzate per l'elaborazione della
`Timetable`. Può comporsi di diversi `Wagon` tutti di una specifica capienza, tipologia e lunghezza.
Nel momento della simulazione il treno viene utilizzato nella creazione del `TrainAgent`.

```mermaid
classDiagram
    direction BT

    class Train {
        <<trait>>
        + name() String
        + length() Int
        + lengthSize() Int
        + maxSpeed() Int
        + capacity)() Int
    }

    class Wagon {
        <<trait>>
        + use: UseType
        + capacity: Capacity
        + lengthSize: LengthMeter
    }

    class WagonType {
        <<enumeration>>
        Passenger
        Other
    }

    class TrainTechnology {
        <<trait>>
        + acceleration() Double
        + deceleration() Double
    }

    class Technology {
        <<trait>>
        + name() String
        + maxSpeed() Int
        + isCompatible(other: Technology)
    }

    TrainTechnology --|> Technology
    TrainTechnology ..o Train
    Wagon ..o Train
    WagonType ..o Wagon
```

### TrainAgentStates (Behaviour)
L'entità `StateBehavior` rappresenta lo stato immutabile del `TrainAgent` durante la simulazione. Le informazioni del moto del treno vengono modellate con `MotionData` e vengono aggionrate ad ogni step della simualzione in base allo stato in cui si trova il treno.

```mermaid
classDiagram
direction BT
    class TrainAgent
    class StateBehavior {
        <<abstract>>
        + motionData() MotionData *
        + stateName() String *
        + enoughSpace(d: Option[Double], train: Train) Boolean *
        + next(train: Train, dt: Int, p: Percepts) *
        + shouldStop(ri: TrainRouteInfo, train: Train) Boolean
        + reset() StateBehavior
        + withOffsetDistance(offset: Double) StateBehavior
    }
    class MotionData {
        + distanceTravelled: Double
        + speed: Double
        + acceleration: Double
        + withAcceleration(acc: Double) MotionData
        + withSpeed(newSpeed: Double) MotionData
        + withDistanceOffset(offset: Double) MotionData
    }
    TrainAgent o.. StateBehavior
    StateBehavior o.. MotionData
```

#### Timetable

L'entità `Timetable` assume un ruolo abbastanza cruciale nella programmazione dei diversi viaggi che i treni devono
svolgere durante la simulazione. Attraverso una sequenza di `Station` e di route `Route` viene definita la tabella
oraria del treno dove per ogni statione viene memorizzato `TrainStationTime`contenente l'ora di arrivo/partenda e minuti
di sosta.
Con questa definizione di viaggio poi, successivamente, vengono ricavate le `DynamicTimetable` necessarie per il
corretto funzionamento della simulazione e in modo particolare dei treni.
Una `Timetable` può essere costruita attraverso il `TimetableBuilder` utilizzando il pattern *Builder Pattern* e
attraverso i metodi che espone è possibile ricavare una `PartialTimetable` (una timetable la cui stazione finale non è
stata ancora definita) o altrimenti una `Timetable`.
Si noti nell' UML sottostante che il builder utilizza un
`TimeEstimator` ([maggiori dettagli](5-impl-violani.md#timetable)), una strategia specifica per il calcolo degli orari
della tabella oraria.

```mermaid
---
title: TimetableBuilders
config:
  class:
    hideEmptyMembersBox: true
---

classDiagram
    direction BT

    class PartialTimetable {
        <<trait>>
        + train() Train
        + startStation() Station
        + departureTime() ClockTime
        + table() ListMap[Station, Time]
    }

    class TrainTimetable {
        <<trait>>
        + arrivingStation() Station
        + arrivingTime() Option[ClockTime]
    }

    class TimetableBuilder {
        <<trait>>
        + stopsIn(station: Station, waitTime: WaitTime)(railInfo: RailInfo) TimetableBuilder
        + transitIn(station: Station)(railInfo: RailInfo) TimetableBuilder
        + arrivesTo(station: Station)(railInfo: RailInfo) TrainTimetable
        + getPartialTimetable(): PartialTimetable
    }

    TrainTimetable --|> PartialTimetable
    TrainTimetableImpl ..|> TrainTimetable: implements
    PartialTrainTimetable ..|> PartialTimetable: implements
    TimetableBuilder ..> TimeEstimator: using
    TimetableBuilder o.. TrainStationTime
    TimetableBuilder ..> PartialTimetable: creates
    TimetableBuilder ..> TrainTimetable: creates
```

## View

Per lo sviluppo della `view`, sono stati progettati componenti personalizzati per
migliorare l'esperienza dell'utente, ottimizzando l'interazione con l'interfaccia.
La progettazione è stata orientata dal principio _SRP_ (Single Responsibility Principle),
con l'obiettivo di ridurre la complessità e la verbosità, delegando a ciascun
componente una responsabilità chiara e specifica nella gestione della creazione grafica.

```mermaid
classDiagram
    direction TB
    class GUI
    class Menu
    class Dashboard

    class SimulationWorkspace
    class MapWorkspace
    class TrainWorkspace

    class StationForm
    class RouteForm
    class TimetableForm
    class MapPanel

    class SimulationForm
    class MapSimulation

    class TrainEditorView

    GUI *--> Menu: 1
    GUI *--> Dashboard: 1
    GUI *--> SimulationWorkspace: 1
    GUI *--> MapWorkspace: 1
    GUI *--> TrainWorkspace: 1
    SimulationWorkspace *--> SimulationForm: 1
    SimulationWorkspace *--> MapSimulation: 1
    MapWorkspace *--> StationForm: 1
    MapWorkspace *--> RouteForm: 1
    MapWorkspace *--> TimetableForm: 1
    MapWorkspace *--> MapPanel: 1
    TrainWorkspace *--> TrainEditorView: 1
```

### Timetable View
L'interfaccia grafica della timetable è composta da una classe *root* la `TimetableView` e due sottoschermate figlie: `EditorTab` e `TimetableViewTab` le quali fanno uso della `TimetableListView` per la visualizzazione delle Timetable (sia per la consultazione che durante la fase di creazione). Le due schermate figlie aderiscono al contratto `TrainsUpdatable` mentre solo la `TimetableViewTab` aderisce anche a `TimetablesUpdatable`.

Il `TimetableViewAdapter` (omesso in questo uml) comunica attraverso questi contratti con le schermate permettendo l'aggiornamento dei dati da visualizzare in modo asincrono.

```mermaid
classDiagram

orientation RL

  class RequestResultObserver {
    <<trait>>
    + showRequestResult(title: String, descr: String)
  }

  class TimetablesUpdatable {
   + updateTimetables(tables: List[Timetable])
  }

  class TrainsUpdatable {
    def updateNewTrains(trains: List[Train])
  }

TimetableViewerTab..|> TrainsUpdatable
EditorTab ..|> TrainsUpdatable
TimetableViewerTab..|> TimetablesUpdatable
TimetableView ..|> RequestResultObserver
TimetableView *--> EditorTab: 1
TimetableView *--> TimetableViewerTab: 1
TimetableViewerTab *--> TimetableListView: 1
EditorTab *--> TimetableListView: 1
```

### Adapters
La `View` comunica con la logica di business tramite `InputAdapter`, che fungono
da intermediari tra l’interfaccia utente e il core applicativo.
Questi adapter sono progettati per garantire un’integrazione efficiente
e trasparente, esponendo i servizi dell’applicazione in conformità alle
InputPort definite a livello applicativo.

Parallelamente, l’applicazione utilizza un `SimulationNotificationListener
per propagare eventi dal dominio verso la View. In questo contesto, l’adapter
SimulationNotificationAdapter si occupa di tradurre le notifiche provenienti
dalla logica di business in un formato compatibile con i componenti grafici,
assicurando un corretto aggiornamento dell’interfaccia in risposta agli eventi
della simulazione.

```mermaid
classDiagram
    direction BT
    class GUI

    class SimulationNotificationListener
    class SimulationNotificationAdapter
    class SimulationPortsOutput

    class SimulationWorkspace
    class MapWorkspace
    class TrainWorkspace

    class SimulationForm
    class MapSimulation
    class InputAdapterManager

    <<Trait>> InputAdapterManager
    <<Trait>> SimulationNotificationListener
    SimulationNotificationListener: updateData(data SimulationData) Unit
    SimulationNotificationListener: endSimulation(data SimulationData) Unit
    InputAdapterManager: train TrainViewAdapter
    InputAdapterManager: train TrainViewAdapter
    InputAdapterManager: timetable TimetableViewAdapter
    InputAdapterManager: station StationEditorAdapter
    InputAdapterManager: route RouteAdapter
    InputAdapterManager: simulationPage SimulationPageAdapter
    InputAdapterManager: simulationInfo SimulationInfoAdapter
    InputAdapterManager <--o GUI: 1
    InputAdapterManager <.. SimulationWorkspace: use
    InputAdapterManager <.. MapWorkspace: use
    InputAdapterManager <.. TrainWorkspace: use
    GUI *--> SimulationWorkspace: 1
    GUI *--> MapWorkspace: 1
    GUI *--> TrainWorkspace: 1
    SimulationWorkspace *--> MapSimulation: 1
    SimulationWorkspace *--> SimulationForm: 1
    SimulationNotificationListener <-- MapSimulation
SimulationNotificationAdapter o--> SimulationNotificationListener
SimulationPortsOutput <-- SimulationNotificationAdapter

```

### Observers

Per quanto riguarda l'interazione con l'utente, è stato adottato il pattern Observer
per monitorare gli eventi grafici, come le azioni del mouse sulla GUI.
Questo approccio consente di osservare gli eventi in tempo reale e, quando
necessario, trasformarli in valori utili per l'applicazione.
Grazie a questo pattern, l'interazione diventa agile, estendibile e facilmente
manutenibile, permettendo una gestione dinamica degli input utente.

```mermaid
classDiagram
    direction BT
    class ClickObserver~T~
    class ReleaseObserver~T~
    class HoverObserver~T~
    class ExitObserver~T~
    class MovedObserver~T~
    class Observer~T~
    class Observable~T~
    <<Trait>> ClickObserver
    <<Trait>> ReleaseObserver
    <<Trait>> HoverObserver
    <<Trait>> ExitObserver
    <<Trait>> MovedObserver
    <<Trait>> Observer
    <<Trait>> Observable
    Observable: attach...(data ...Observer[T]) Unit
    Observable: detach...(data ...Observer[T]) Unit
    Observable: notify...(data T) Unit
    Observable: toObserver[I](newData I => T) Observer[I]
    ClickObserver: onClick(data T) Unit =()
    ReleaseObserver: onRelease(data T) Unit =()
    HoverObserver: onHover(data T) Unit =()
    ExitObserver: onExit(data T) Unit =()
    MovedObserver: onMoved(data T) Unit =()
    Observer --> ClickObserver
    Observer --> ReleaseObserver
    Observer --> HoverObserver
    Observer --> ExitObserver
    Observer --> MovedObserver
    Observable ..|> Observer: transformInto
Observable o--> Observer
Observable o--> ClickObserver
Observable o--> ReleaseObserver
Observable o--> HoverObserver
Observable o--> ExitObserver
Observable o--> MovedObserver
```

### Decorator

La View dell'applicazione è stata progettata per garantire un impatto visivo chiaro
e migliorato, utilizzando il pattern Decorator. Questo pattern permette di
estendere i componenti grafici nativi senza modificarne la struttura originale,
migliorando la resa grafica complessiva. In questo modo, è stato possibile
arricchire l'interfaccia con funzionalità visive aggiuntive, mantenendo al
contempo la modularità e la riusabilità dei componenti di base.

```mermaid
classDiagram
    direction BT
    class Observable~MouseEvent~
    class EnhancedLook
    class ShapeEffect
    class BorderEffect
    class FontEffect
    class ImageEffect
    class PictureEffect
    class SVGEffect
    <<Trait>> Observable~MouseEvent~
    <<Trait>> EnhancedLook
    <<Trait>> ShapeEffect
    <<Trait>> BorderEffect
    <<Trait>> FontEffect
    <<Trait>> ImageEffect
    <<Trait>> PictureEffect
    <<Trait>> SVGEffect
    ImageEffect <-- PictureEffect
    ImageEffect <-- SVGEffect
    EnhancedLook <-- ShapeEffect
    EnhancedLook <-- BorderEffect
    EnhancedLook <-- FontEffect
    EnhancedLook <-- ImageEffect
    EnhancedLook <-- Observable
```

## Railway simulator

Un'altra parte centrale del progetto riguarda la simulazione della rete ferroviaria, in questa sezione sarà descritta
nel dettaglio questa parte di architettura.

### Entities

Dato che si è sviluppato un simulatore agent-based la prima suddivisione che si definisce per le entità è quella di:

- Environment: lo spazio in cui gli agenti esistono e operano
- Agents: le entità autonome del sistema che interagiscono con l'ambiente attraverso un processo continuo di
  sense-decice-act

Il componente di base per l'Environment è l'`EnvironmentElement` che può a sua volta contenere o meno degli `Agents`.
Nel caso in cui contenga degli agents questo farà utilizzo di un `SimulationAgentContainer`. Per limitare la complessità
necessaria
alla modellazione del sistema si è scelto di costruire la gerarchia dei `SimulationAgentContainer` a partire dai
`TrainAgent`
definendo il componente `TrainAgentContainer`. Tra questi container sono stati definiti:

- **Platform**: container che rappresenta una rotaia della Station, occupabile da un treno alla volta
- **Track**: container che rappresenta una rotaia di una Route, dato che può essere utilizzata
  da più treni contemporaneamente è stato necessario definirne il verso di percorrenza

```mermaid
---
config:
    class:
        hideEmptyMembersBox: true
---
classDiagram
    direction TB
    class EnvironmentElement
    class TrainAgentEEWrapper {
        + updateTrain(TrainAgent) Option[TrainAgentEEWrapper]
        + removeTrain(TrainAgent) Option[TrainAgentEEWrapper]
    }
    class TrainAgentContainers {
        + updateTrain(TrainAgent) Option[TrainAgentContainers]
        + removeTrain(TrainAgent) Option[TrainAgentContainers]
    }

    class TrainAgent
    class SimulationAgent {
        + doStep(dt: Int, EnvironmentCoordinator) SimulationAgent
    }
    class Train
    class Platform {
        + putTrain(TrainAgent) Option[Platform]
    }
    class TrackDirection {
        case Forward, Backward
    }
    <<enum>> TrackDirection
    class Track {
        + putTrain(TrainAgent, TrackDirection) Option[Track]
    }

    <<trait>> EnvironmentElement
    <<trait>> TrainAgentEEWrapper
    <<trait>> TrainAgentContainers
    <<trait>> TrainAgent
    <<trait>> SimulationAgent
    <<trait>> Train
    <<trait>> Platform
    <<trait>> Track
    EnvironmentElement <-- TrainAgentEEWrapper
    TrainAgentEEWrapper o-- TrainAgentContainers
    TrainAgent --> SimulationAgent
    TrainAgent --> Train
    TrainAgentContainers o-- TrainAgent
    Track o-- TrackDirection
    Track --> TrainAgentContainers
    Platform --> TrainAgentContainers
```

Complessivamente gli `EnvironmentElement` sono:

- **StationEnvironmentElement**: rappresenta una Station nell'ambito della simulazione, ed è composta da una o più
  `Platform`
- **RouteEnvironmentElement**: rappresenta una Route nell'ambito della simulazione, ed è composta da una o più `Track`
- **DynamicTimetable**: rappresenta le timetable nell'ambito della simulazione, rispetto alla controparte statica è
  composta da una tabella degli orari effettiva e offre funzionalità per l'aggionamento di quest'ultima

```mermaid
classDiagram
    direction LR
    class EnvironmentElement
    class DynamicTimetable {
        + effectiveTable List [~Station, TrainStationTime~]
        + arrivalUpdate(ClockTime) Option[DynamicTimetable]
        + departureUpdate(ClockTime) Option[DynamicTimetable]
    }
    class TimeTable
    class RouteEnvironmentElement {
        + putTrain(TrainAgent, TrackDirection) Option[RouteEnvironmentElement]
    }
    class Route
    class StationEnvironmentElement {
        + putTrain(TrainAgent) Option[StationEnvironmentElement]
    }
    class Station
    class TrainAgentEEWrapper
    class Track
    class Platform

    <<trait>> EnvironmentElement
    <<trait>> DynamicTimetable
    <<trait>> TimeTable
    <<trait>> RouteEnvironmentElement
    <<trait>> Route
    <<trait>> StationEnvironmentElement
    <<trait>> Station
    <<trait>> TrainAgentEEWrapper
    <<trait>> Track
    <<trait>> Platform
    DynamicTimetable --> TimeTable
    DynamicTimetable --> EnvironmentElement
    RouteEnvironmentElement --> Route
    RouteEnvironmentElement --> TrainAgentEEWrapper
    StationEnvironmentElement --> Station
    StationEnvironmentElement --> TrainAgentEEWrapper
    EnvironmentElement <-- TrainAgentEEWrapper
    StationEnvironmentElement ..> Platform: Use
    RouteEnvironmentElement ..> Track: Use

```

Si può notare l'applicazione del principio `SRP` in quanto ogni entità legata alla simulazione
estende le funzonalità della controparte statica ed inoltre anche nel caso degli `EnvironmentElement`, come si può
notare dai grafici riportati, ogni elemento della gerarchia si occupa di effettuare uno specifico sotto insieme di
controlli.

Gli `EnvironmentElement` sono a loro volta raggruppati in `Environment` così da agevolarne la gestione e analogamente
anche questi ultimi possono o meno contenere degli `EnvironmentElement`, anche in questo caso, per motivi analoghi a
quelli precedenti,
la gerarchia parte dal considerare i `TrainAgentEnvironment`.

In particolare il `DynamicTimetableEnvironment` è responsabile della ricerca e aggiornamento delle `DynamicTimetable` a
partire da un `TrainAgent`, un `ClockTime` e una funzione di update.

```mermaid
classDiagram
    direction TB
    class EnvironmentElement
    class DynamicTimetable
    class Environment {
        + environmentElements Seq[EnvironmentElements]
    }
    class DynamicTimetableEnvironment {
        + updateTables(updateFunction, agent, time) Option[DynamicTimetableEnvironment]
    }

    <<trait>> EnvironmentElement
    <<trait>> DynamicTimetable
    <<trait>> Environment
    <<trait>> DynamicTimetableEnvironment

    Environment --o EnvironmentElement
    DynamicTimetable --> EnvironmentElement
    DynamicTimetableEnvironment --> Environment
    DynamicTimetableEnvironment ..> DynamicTimetable: Use

```

Riguardo i `TrainAgentEnvironment` si trovano invece i:

- **StationEnvironment**: responsabile della gestione dei `TrainAgent` negli `StationEnvironmentElement`, in particolare
  per l'ingresso e uscita dei Train dalle Platform
- **RouteEnvironment**: responsabile della gestione dei `TrainAgent` nei `RouteEnvironmentElement`, in particolare per
  il movimento dei Train e per l'ingresso e uscita dalle Track

```mermaid
classDiagram
    direction TB

    class RouteEnvironmentElement
    class StationEnvironmentElement
    class TrainAgentEEWrapper
    class Environment
    class TrainAgentEnvironment {
        + updateTrain(TrainAgent) Option[TrainAgentEnvironment]
        + removeTrain(TrainAgent) Option[TrainAgentEnvironment]
    }
    class StationEnvironment {
        + putTrain(TrainAgent, Station) Option[StationEnvironment]
    }
    class RouteEnvironment {
        + putTrain(TrainAgent, (Station, Station)) Option[RouteEnvironment]
    }

    <<trait>> RouteEnvironmentElement
    <<trait>> StationEnvironmentElement
    <<trait>> TrainAgentEEWrapper
    <<trait>> Environment
    <<trait>> TrainAgentEnvironment 
    <<trait>> StationEnvironment 
    <<trait>> RouteEnvironment 

    TrainAgentEnvironment --> Environment
    TrainAgentEnvironment ..> TrainAgentEEWrapper: Use
    RouteEnvironment --> TrainAgentEnvironment
    RouteEnvironment ..> RouteEnvironmentElement: Use
    StationEnvironment --> TrainAgentEnvironment
    StationEnvironment ..> StationEnvironmentElement: Use

```

A conclusione della modellazione della gerarchia delle entità della simulazione si trova l'`EnvironmentCoordinator` che
si occupa di coordinare l'aggiornamento dei diversi `SimulationAgents` e le successive operazioni di aggiornamento
all'interno dei diversi `Environment`.
I `SimulationAgents` a loro volta si aggiorneranno in ogni step basandosi sulle informazioni fornite dall'
`EnvironmentCoordinator`. Per rappresentare il nostro dominio è stato definito un particolare tipo di coordinatore: il
`RailwayEnvironment`.

```mermaid
---
  config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
    class EnvironmentsCoordinator {
        + doStep(dt: Int) EnvironmentsCoordinator
        + environments() Seq[Environment[?]]
        + perceptionFor(SimulationAgent) Option[Perception]
    }
    class RailwayEnvironment

    class Environment

    class SimulationAgent {
        type EC <: EnvironmentsCoordinator[EC]
        + doStep(dt: Int, environment: EC) SimulationAgent
    }

    <<trait>> EnvironmentsCoordinator
    <<trait>> RailwayEnvironment
    <<trait>> Environment
    <<trait>> SimulationAgent
    RailwayEnvironment --> EnvironmentsCoordinator
    Environment --o EnvironmentsCoordinator
    SimulationAgent --o EnvironmentsCoordinator
    SimulationAgent ..> EnvironmentsCoordinator: dependsOn
``` 

### Simulation Engine

L'Engine è il cuore del sistema di simulazione, responsabile del suo avanzamento e della gestione del tempo di
esecuzione.

#### Entities

Lo stato della simulazione è mantenuto attraverso due strutture dati dedicate:

- **EngineState**: traccia l'ultimo aggiornamento, il delta temporale e il tempo di ciclo accumulato.
- **SimulationData**: contiene informazioni sullo step corrente, sul tempo di simulazione trascorso e sullo stato del
  RailwayEnvironment.

```mermaid
---
config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
    direction BT

    class RailwayEnvironment
    class SimulationData {
        + step: Int
        + millisecondsElapsed: Milliseconds
    }
    class Engine {
        + running: Boolean
    }
    class EngineState {
        + lastUpdate: Option[Long]
        + lastDelta: Long
        + elapsedCycleTime: Long
    }
    class EngineConfiguration {
        + stepSize: Int
        + cyclesPerSecond: Option[Int]
    }

    <<trait>> RailwayEnvironment
    <<trait>> SimulationData
    <<trait>> Engine
    <<trait>> EngineState
    <<trait>> EngineConfiguration
    RailwayEnvironment --o SimulationData
    EngineState --o Engine
    EngineConfiguration --o Engine

```

#### Application

A livello dell'applicazione è presente il `SimulationManager`, che comunicando con un `TimeProvider`
e avendo la capacità di inviare notifiche attraverso un `NotificationService`
ha il compito di coordinare le varie entità della simulazione e gestirne attivamente l'avanzamento.

Per quanto riguarda il `NotificationService` risulta banale la sua implementazione come output port, definendo una ben
chiara necessità di interazione con l'esterno.
Il `TimeProvider` invece si è scelto di definirlo come porta di output, nonostante si sarebbe potuta usare internamente
l'ulility `System.currentTimeMillis()`, per mantenere le fonti di randomicità all'esterno, rispettando così i principi
dell'architettura esagonale e agevolando il successivo testing dell'engine attraverso un `FakeTimeProvider`

```mermaid
---
config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
    direction BT
    class NotificationService {
        + stepNotification(SimulationData) Unit
        + simulationEnded(SimulationData) Unit
    }
    class TimeProvider {
        + currentTimeMillis() Long
    }
    class AppState
    class SimulationManager {
        + start() SimulationManager
        + stop() SimulationManager
        + reset() SimulationManager
        + doStep() SimulationManager
    }

    class SimulationData
    class Engine


    <<trait>>NotificationService
    <<trait>>TimeProvider 
    <<trait>>AppState
    <<trait>>SimulationManager
    <<trait>>SimulationData
    <<trait>>Engine

    NotificationService --o SimulationManager
    TimeProvider --o SimulationManager
    SimulationManager --o AppState
    SimulationData --o SimulationManager
    Engine --o SimulationManager
    EngineState --o Engine

```
