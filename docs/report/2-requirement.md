
# Requisiti 
Di seguito viene definita la vision del progetto e i diversi requisiti che dovrà soddisfare.
## Business
Si vuole sviluppare un simulatore di reti ferroviarie, progettato per offrire un 
ambiente dedicato a test ed analisi. Le principali finalità includono:

- Stima del comportamento del sistema: analizzare e prevedere le prestazioni del sistema ferroviario 
in relazione a diverse configurazioni operative
- Test di prototipi: consentire la verifica sperimentale di nuove configurazioni e soluzioni progettuali 
prima della loro implementazione reale
- Ottimizzazione delle prestazioni: fornire dei KPI per valutare l'efficienza della rete, 
ad esempio riportando i ritardi accumulati

### Modello di dominio
Gli elementi principali del sistema sono:
- **Station**: punto di partenza e arrivo di un Train
- **Platform**: rotaia all'interno della Station
- **Train**: veicolo su rotaia che trasporta persone o merci
- **Technology**: tecnologia utilizzata da un Train o da una Route
- **Route**: tratta fra due Station, che può contenere una o più Track
- **Track**: collegamento fisico tra due Station
- **Direction**: verso corrente di percorrenza di una Track
- **Timetable**: definizione degli orari di partenza-attesa-arrivo di un Train
- **Statistics**: dati relativi alla Simulation (ritardi, occupazione Station, ...)
- **Railway**: aggregazione di Station e Route
- **Simulation**: esecuzione di un determinato scenario in una Railway

![An image](/resources/requirements/domainModel.svg)

## Funzionali
### Utente
- L'utente deve poter interagire con l'applicazione tramite un'interfaccia 
- L'utente deve poter creare una nuova Railway o modificarne una esistente potendo specificare e combinare 
liberamente le varie Station e Route, definendo anche i Train e le Timetable
- L'utente deve poter vedere l'esecuzione della Simulation una volta avviata
- L'utente deve poter vedere statistiche/info di un Train, Station e Route durante la Simulation
- L'utente deve poter vedere le Timetable programmate per un Train
- L'utente deve poter avviare, mettere in pausa o fare il reset della Simulation
- L'utente deve poter visualizzare gli errori e avvisi prodotti dal sistema

### Sistema
- Il sistema deve poter visualizzare la Railway
- Il sistema deve poter visualizzare le infomazioni delle entità della Simulation
- Il sistema deve poter gestire la creazione/modifica/rimozione di Station, Route, Timetable e Train
- Il sistema deve poter gestire l'esecuzione della Simulation
  - evitando la sovrapposizione dei Train in una Route
  - riconoscendo la presenza di ritardi
- Il sistema deve poter gestire il calcolo delle statistiche relative alla Simulation
- Il sistema deve poter controllare la correttezza del setup della Simulation
- Il sistema deve poter replicare una stessa Simulation
- Il sistema deve poter gestire errori e avvisi

## Non funzionali
- Usabilità: l'interfaccia grafica deve essere semplice e intuitiva
- Robustezza: l'applicazione deve gestire setup errati
- Affidabilià: l'applicazione deve essere stabile, evitando crash
- Testabilità: devono essere presenti test per verificare il corretto funzionamento del sistema
- Manutenibilità: il codice deve essere ben strutturato e ben documentato
- Estendibilità: il progetto deve favorire la personalizzazione e l'aggiunta di funzionalità
- Portabilità: l'applicazione può essere eseguita su più sistemi operativi

## Implementazione
- Scala 3.3.4
- ScalaTest 3.2.19
- ScalaSwing 3.0.0
- TDD
- SBT as automation tool 1.10.1
- SBT assembly (FAT jar) 2.3.0 
- Scaladoc
- Scalafmt 2.5.2
- ArchUnit 1.3.0
- Wartremover 3.2.5
- Semantic release
- Scoverage 2.2.2
- Immutabilità dell'applicazione

## Opzionali
- introduzione di randomness controllata all'interno della simulazione
- scelta della tipologia di alimentazione del Train e gestione dell'autonomia
- ottimizzazione/ricerca dinamica del percoso più veloce (vengono definite solo le Station dove si ferma 
il Train e il resto dei percorsi è gestito da un sistema di balancing del traffico)
- selezione del tipo di sosta in una Station (breve/lunga)
- cambiare colore agli elementi della Railway a seconda del loro stato(e.g. se una Station è quasi piena 
sarà mostrata diversamente da una Station libera) 
- mostrare dei box con delle statistiche sopra le entità della Simulation
- gestione personalizzabile della strategia decisionale dei Train (e.g. criteri di priorità per 
la selezione di una Track)
- gestione di eventi esterni che influiscono su porzioni della Railway (e.g. pioggia, neve, scioperi, ecc...)