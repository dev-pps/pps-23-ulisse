# Processo di sviluppo
Di seguito vengono illustrate la metodologia di sviluppo adottata, il sistema 
di versionamento con le relative convenzioni utilizzate e le pipeline di CI/CD implementate.

## Metodologia
È stato adottato un processo di sviluppo SCRUM-inspired come suggerito, 
con il fine di garantire una gestione agile e iterativa delle attività progettuali.

Durante l’intero ciclo di vita del progetto, la qualità del processo è migliorata parallelamente alla crescita del team di sviluppo, che ha affrontato con successo le sfide progettuali, rafforzando nel tempo la propria sinergia e coesione.

### Ruoli
**Product Owner**: Manuel J. Buizo, responsabile del monitoraggio del progresso del progetto, dell’allineamento rispetto agli obiettivi aziendali e del coordinamento del team di sviluppo, e sviluppatore.

**Scrum Master**: Matteo Violani, il cui ruolo è quello di verificare la corretta applicazione della metodologia scrum e di aiutare a rimuovere gli impedimenti che il Team e il Product Owner hanno identificato durante il processo di sviluppo, e sviluppatore.

**Stakeholder**: Federico Bravetti, committente e responsabile della qualità e usabilità del prodotto, e sviluppatore.

### Meeting
Le tipologie di meeting utilizzate sono le seguenti:

- **Initial planning**: il kickoff meeting ha coinvolto l'intero team per definire il 
Product Backlog, stabilire gli obiettivi del primo sprint e selezionare le tecnologie 
più appropriate per il progetto, in base alle necessità tecniche e agli obiettivi di 
business a lungo termine.


- **Sprint review e retrospettiva**: durante lo sprint review vengono presentati i risultati raggiunti allo stakeholder e una volta ispezionati vengono identificati i possibili miglioramenti. Successivamente si passa alla retrospettiva dove vengono pianificati modi per incrementare la qualità del processo e l’efficacia del team.


- **Sprint planning**: meeting di pianificazione in cui vengono selezionate le attività da svolgere nel prossimo sprint e ne viene definito il goal.


- **Daily Scrum**: aggionamento della durata di circa 15/30 minuti, svolto quasi ogni giorno 
con lo scopo di mantenere aggiornati tutti i membri del team sullo stato di avanzamento 
del progetto ed eventuali problematiche riscontrate. 
In caso di necessità sono state invece organizzate delle vere e proprie riunioni per discutere 
e analizzare meglio alcuni aspetti progettuali.


### Youtrack
Per la gestione del Product Backlog, degli sprint e delle attività da svolgere, è stato scelto lo strumento YouTrack.
Durante ogni Sprint Planning sono state create le nuove issues da svolgere nello sprint, in particolare:
- le issue più rilevanti sono state immediatamente assegnate ai membri del team
- le issue rimanenenti sono state selezionate in autonomia dai membri che hanno completato i propri task
- le issue rimaste incomplete nello sprint precedente sono state trasferite nello sprint corrente
- se necessario le issue più complesse sono state suddivise successivamente in task più piccoli

## DVCS
Per quanto riguarda l'utilizzo di Git e GitHub, è stato deciso di adottare il modello GitFlow, 
andando ad utilizzare diversi branch per la gestione del codice sorgente.
In particolare è stato usato `main` come branch principale su cui è stato effettuato il 
rebase degli altri branch, in modo da avere una storia dei commit lineare.


### Conventional commit
Per la scrittura del messaggio di commit è stata utilizzata la convenzione [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/). 
Inoltre per rafforzare l'adesione a questa convenzione è stato impostato un commit-msg 
hook tramite build del progetto e poi rafforzata definitivamente con una pipeline di CI/CD.

### Conventional branch
Per la denominazione dei branch è stata utilizzata la convenzione [Conventional Branch](https://conventional-branch.github.io/).

### Branch Protection
Per effettuare il rebase degli altri branch su `main`, è stato introdotto un sistema di 
branch protection che prevede l'utilizzo di pull request con la richiesta di review 
da parte degli altri due membri del team.

In questo modo, è stata garantita una maggiore qualità del codice, promuovendo anche 
una maggiore awareness riguardo lo stato del progetto. La revisione del codice ha
facilitato il confronto e l'apprendimento continuo tra i membri del team.

## Definition of Done
Una feature è considerata finita ed integrabile in `main` se soddisfa i seguenti criteri:
- Sono eseguite con successo le pipeline di CI/CD, che controllano:
  - building del progetto
  - testing del progetto 
  - formattazione del codice
  - correttezza della convezione utilizzata per i commit
- Il codice è stato correttamente documentato

## Testing
Per garantire la qualità e la correttezza delle funzionalità implementate, 
è stato adottato il paradigma Test Driven Development (TDD). 
Questo approccio permette di identificare e correggere tempestivamente eventuali 
bug a livello di singoli componenti durante le fasi di sviluppo, assicurando 
un ciclo di feedback continuo.

Il processo di sviluppo con TDD prevede tre passaggi principali:

1. Fase Red (testing): si scrive un test che descrive il comportamento atteso del 
componente o della funzionalità. Poiché l'implementazione non è ancora presente, 
il test fallisce inizialmente.

2. Fase Green (implementation): successivamente, si procede con l'implementazione 
del componente o della funzionalità, in modo da garantire che il test precedentemente 
scritto venga superato con successo.

3. Fase Refactor: dopo il superamento del test, si procede con il refactoring del 
codice per migliorarne la qualità e la leggibilità, assicurandosi che il test rimanga 
positivo.


## Building
Come build tool è stato scelto Sbt, per gestire:
- Le dipendenze del progetto
- La configurazione degli hook scelti
- L'esecuzione dei test
- La generazione della documentazione
- La creazione degli artefatti
- La produzione del report di coverage

### Code Quality
Per garantire la qualità del codice sono stati utilizzati i seguenti strumenti:
- Scalafmt, per garantire la qualità e la formattazione automatica del codice. 
L'esecuzione della formattazione è stata inoltre integrata in un pre-commit hook.
- Wartremover, per favorire una corretta adesione al paradigma funzionale. Sono stati utilizzati come errori tutti i `Warts.unsafe` e come warning tutti 
i `Warts.all` ad eccezione di alcuni quali `Wart.ImplicitParameter` e `Wart.Equals`.

### CI/CD
Per la Continuous Integration e Continuous Delivery è stato scelto di utilizzare le 
GitHub Actions.
In particolare, sono stati definiti i seguenti workflow:

- BuildAndTest: esegue la build del progetto e il lancio dei test su una matrice [java, os]

- ValidateCommit: esegue un controllo della correttezza della convenzione definita per i commit

- DeployReportSite: generazione della documentazione con rispettiva scaladoc e coverage, con upload su GitHub Pages

- Release: rilascio della nuova versione del progetto su GitHub utilizzando il tool Semantic Release, comprendendo la generazione e l'inclusione del jar del progetto tra gli asset di quest'ultima.

>NOTA: la pipeline di release è attiva solo in seguito all'accettazione di una pr su main, mentre la pipeline DeployReportSite è attiva solo in main e nei branch docs/*.

## Report
Per il report del progetto è stato utilizzato VitePress, un generatore di siti statici basato su Vue.js che ha permesso la generazione e caricamento su Github Pages del report in modo semplice e veloce, utilizzando file Markdown.