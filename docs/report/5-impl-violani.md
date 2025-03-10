# Implementazione di Violani Matteo



## Train & Technology
- definizione e sviluppo della entità `Train` e di `TrainTechnology` (sottotipo di `Technology`), le classi come `Wagons` e `Train` non sono così entusiasmanti
- `TrainAgent` e il suo compornamento dinamico (FSM) in base ai percepts ricevuti.
  - MotionData
  - TrainAgentsStates (concetto base `StateBehavior` con mixins trait per rendere moluare la logica di fermata con metodo `shouldStop` e il calcolo delle distanza di sicurezza fornita dal metodo `enoughSpace` del trait `SpaceManagement`)

## Timetable
- sviluppo della entità in se, con metologia TDD, 

### Creazione DSL per la definizione di una Timetable
Risultato finale ottenuto grazie al dsl.
```scala 3
AV1000Train at h(9).m(0).getOrDefault startFrom stationA thenOnRail
  railAV_10 andStopIn stationB waitingForMinutes 5 thenOnRail
  railAV_10 travelsTo stationC thenOnRail
  railAV_10 andStopIn stationD waitingForMinutes 10 thenOnRail
  railAV_10 arrivesTo stationF
```

Utilizzando il builder in modo classico:
```scala 3
TimetableBuilder(train = AV1000Train, startStation = stationA, departureTime = h(9).m(0).getOrDefault)
  .stopsIn(stationB, waitTime = 5)(railAV_10)
  .transitIn(stationC)(railAV_10)
  .stopsIn(stationD, waitTime = 10)(railAV_10)
  .arrivesTo(stationF)(railAV_10)
```