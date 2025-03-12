# Architettura

Come pattern architetturale è stata adottata l'Architettura Esagonale (_Ports and Adapters_), 
che isola il core domain (_entities_) dalle interfacce esterne, garantendo un disaccoppiamento 
tra logica di business e meccanismi di input/output. 
Questo favorisce la testabilità, l'estendibilità e la manutenibilità del codice, permettendo 
inoltre di sostituire o evolvere le interfacce utente e i servizi esterni senza impattare il
dominio applicativo, data l'indipendenza dalle tecnologie adottate nei layer periferici.

![An image](/resources/architecture/architecture.svg)

Seguendo la convenzione comune adottata per l'architettura esagonale, il progetto si struttura nel seguente modo:

- **entities**: questo package definisce gli elementi statici, utilizzati per la configurazione della Railway, e quelli dinamici, impiegati nella simulazione per gestirne l'evoluzione.
  Inoltre è stato definito qui anche il modello dell'engine di simulazione e il modulo di supporto per il calcolo delle statistiche

- **application**: in questo livello risiedono le logiche di business fondamentali del sistema, responsabili 
della gestione delle entità di dominio, dalla creazione alla manipolazione, e del controllo dell’intera 
simulazione. Tutto lo stato del dominio è centralizzato e rappresentato in modo immutabile all’interno 
di `AppState`, che incapsula l’intera configurazione e l’evoluzione dello stato del sistema.
Le interazioni con il dominio avvengono attraverso porte di input e di output, che fungono da contratti per l'invio e 
la ricezione di comandi e dati, mantenendo l'isolamento dal mondo esterno, in particolare si trovano:
  - **inputPorts**: le porte di input definiscono le operazioni consentite per la modifica delle entità del sistema e 
  il controllo dello stato della simulazione. Queste sono poi implementate degli `useCases` i quali 
  rappresentano i servizi offerti dal sistema.
  
  - **outputPorts**: le porte di output, invece, rappresentano le interfacce astratte utilizzate dal dominio 
  per richiedere servizi o per interagire con componenti esterni.

In questo modo, l'`AppState` e la logica di dominio rimangono indipendenti dalle implementazioni esterne 
all'applicazione, rispettando i principi di inversione delle dipendenze (_DIP_), di segregazione 
dell'interfaccia (_ISP_), e favorendo un'architettura decoupled e testabile.

- **adapters**: sono qui definiti gli adapter del sistema, ossia quei componenti che hanno il compito di adattare le richieste esterne o verso l'esterno, in particolare si trovano:
  
  - **inputAdapters**: sono utilizzati per interagire con le porte di input e quindi usufruire dei servizi offerti dall'applicazione. Nel nostro caso questi adapter sono utilizzati per adattare le richieste della gui riguardo la gestione delle entità e il controllo della simulazione. 
  
  - **outputAdapters**: sono utilizzati dall'applicazione per comunicare verso servizi esterni come la gui, per l'invio di notifiche riguardanti la simulazione, o il `TimeProvider`, per richiedere le informazioni relative al tempo corrente. 

- **infrastructures**: in questo package sono definite le interfacce utente e i servizi esterni, in particolare, come riportato nel diagramma in figura, è presente l'interfaccia sviluppata in ScalaSwing e un `TimeProvider`.

Per la verifica rigorosa delle dipendenze strutturali del progetto, è stato adottato il 
framework **ArchUnit**, così da garantire il rispetto delle regole architetturali definite.  
Inoltre, sono stati sviluppati test per verificare il rispetto delle convenzioni adottate sui nomi 
delle classi all'interno dei specifici package, assicurando la conformità ai formalismi 
architetturali definiti.


