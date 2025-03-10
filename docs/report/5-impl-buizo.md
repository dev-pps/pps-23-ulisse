# Manuel Buizo

Il codice implementato durante l’esecuzione del progetto è stato prevalentemente indirizzato alle seguenti aree
funzionali:

- Gestione dell’immutabilità e dei meccanismi di aggiornamento dello stato dell’applicazione
- Implementazione dell’entry point e gestione delle porte di comunicazione con i componenti applicativi
- Progettazione, costruzione e validazione delle entità route
- Definizione e creazione degli elementi infrastrutturali tramite Domain-Specific Language (DSL)
- Progettazione e implementazione dell’interfaccia grafica utente (GUI), inclusa la visualizzazione della mappa e la
  gestione degli eventi interattivi
- Rendering grafico della simulazione, comprensivo di stazioni route e train

Di seguito si propone un’analisi più approfondita delle componenti più rilevanti.

## Immutabilità e aggiornamento dello stato: `AppState`

**Obiettivo**: Creare uno stato immutabile dell'applicazione e garantire
la sua coerenza e integrità.

**Motivazione**: L'adozione di funzioni per la modifica dello stato, anziché
interventi diretti, promuove una gestione funzionale basata sull'immutabilità
e riduce i side effects.
Questo approccio semplifica il testing e garantisce un controllo più rigoroso
e una maggiore coerenza nella gestione dello stato.

### Componente

Di seguito è mostrata una rappresentazione parziale delle classi che compongono `AppState`.

```mermaid
classDiagram
    class AppState
    class StationManager
    class RouteManager
    class TrainManager
    <<Trait>> AppState
    <<Trait>> StationManager    
    <<Trait>> RouteManager
    <<Trait>> TrainManager
   
    AppState : updateStationManager(update StationManager => StationManager) AppState
    AppState : updateRouteManager(update RouteManager => RouteManager) AppState
    AppState : updateTrainManager(update TrainManager => TrainManager) AppState
    
    StationManager --o AppState
    RouteManager --o AppState
    TrainManager --o AppState
```

### Descrizione tecnica

Sfruttando la funzione `copy` delle case class di Scala, è possibile creare un nuovo
stato dell'applicazione a partire da uno stato precedente, modificando solo le parti
interessate. Le funzioni di aggiornamento dello stato, definite all'interno della classe
`AppState`, accettano come argomento una funzione che modifica i componenti interessati.

```Scala 3
override def updateStationManager(update: StationManager => StationManager): AppState =
  copy(stationManager = update(stationManager))

override def updateRoute(update: RouteManager => RouteManager): AppState =
  copy(routeManager = update(routeManager))
```

## Entry point e porte di comunicazione: `EventQueue`

**Obiettivo**: Implementare un sistema di comunicazione per leggere e aggiornare
lo stato dell'applicazione (`AppState`) dell'esterno, preservando l'immutabilità.

**Motivazione**: Gestire in modo efficiente e sicuro le interazioni tramite le porte
dell'architettura esagonale, implementando un sistema di comunicazione robusto e
scalabile, che assicura una gestione funzionale della creazione e dell'aggiornamento
dello stato dell'applicazione.

### Componente

La definizione di una coda dedicata per ciascun concetto di `Service` consente di aderire
ai principi di *Single Responsibility SRP* e *Dependency Inversion DIP*, assicurando
alta coesione e basso accoppiamento. Questo approccio favorisce la modularità e migliora
la scalabilità dell'architettura.  
Di seguito è riportata una porzione delle classi coinvolte.

```mermaid
classDiagram
    class AppState
    class EventQueue
    class StationEventQueue
    class RouteEventQueue
    class TrainEventQueue
    class StationService
    class RouteService
    class TrainService
    <<Trait>> EventQueue
    <<Trait>> StationEventQueue
    <<Trait>> RouteEventQueue
    <<Trait>> TrainEventQueue
    <<Trait>> StationService
    <<Trait>> RouteService
    <<Trait>> TrainService
    EventQueue: LinkedBlockingQueue[AppState => AppState] events
    EventQueue: startProcessing(initState AppState) Unit

    EventQueue *--> AppState
    StationEventQueue <-- EventQueue
    RouteEventQueue <-- EventQueue
    TrainEventQueue <-- EventQueue
    StationService *--> StationEventQueue
    RouteService *--> RouteEventQueue
    TrainService *--> TrainEventQueue
```

### Descrizione tecnica

L'`EventQueue` è una coda bloccante di funzioni che aggiornano lo stato dell'applicazione.
Il metodo `startProcessing` utilizza` LazyList.continually` per estrarre continuamente
eventi dalla coda. Ogni evento, rappresentato da una funzione, viene applicato
allo stato corrente tramite `foldLeft`, producendo una nuova versione immutabile dello
stato ad ogni iterazione.

```Scala 3
override def startProcessing(initState: AppState): Unit =
  LazyList.continually(events.take).foldLeft(initState)((state, fun) => fun(state))
```

## Entità route: `Route`

**Obiettivo**: Progettare e implementare le entità che rappresentano i percorsi
tra le stazioni, assicurando che la creazione delle rotte avvenga secondo vincoli
di validità definiti.

**Motivazione**: Definire un sistema per la creazione delle `Route` che garantisca
una costruzione sicura e funzionale dell'entità, sfruttando le capacità di Scala
per la gestione degli errori e la validazione dei dati in modo dichiarativo.

### Componente
Di seguito sono riportate alcune funzionalità per la modifica e la 
validazione dei campi di `Route`.

```mermaid
classDiagram
    class Route
    Route: withArrival(arrival Station) Either[RouteError, Route]
    Route: withRailsCount(railsCount Int) Either[RouteError, Route]
    Route: withLength(length Double) Either[RouteError, Route]
```
### Descrizione tecnica
Spiegare che 
```scala 3
type RouteError = NonEmptyChain[Errors]

object Route:
  def apply(departure: Station, arrival: Station, typeRoute: RouteType,
            railsCount: Int, length: Double): Either[RouteError, Route]
```

## DSL per la creazione della railway

## mixin decorator: graphics

# observer gui pattern

# testing della route adapter

**Obiettivo**

**Motivazione**

### Componente

### Descrizione tecnica

### Pattern

### Testing

### Criticità
