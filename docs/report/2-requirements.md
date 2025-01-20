# Requisiti 

## Business
Si vuole sviluppare un simulatore di reti ferroviarie, progettato per offrire un 
ambiente dedicato a test e analisi. Le principali finalità includono:

- Stima del comportamento del sistema: analizzare e prevedere le prestazioni del sistema ferroviario 
in relazione a diverse configurazioni operative.
- Test di prototipi: consentire la verifica sperimentale di nuove configurazioni e soluzioni progettuali 
prima della loro implementazione reale.
- Ottimizzazione delle prestazioni: fornire dei KPI per valutare l'efficienza della rete, 
ad esempio riportando i ritardi accumulati.
- Valutazione di scenari critici: studiare gli effetti di eventi come interventi di manutenzione programmata 
o guasti imprevisti.

## Modello di dominio

- Stazione
  - binari
- treno 
  - normali, ad alta velocità
  - velocità max nominale
  - num carrozze => capacità (se c'è un tipo associato alle carrozze)
- carrozze
  - lunghezza
  - tipo? (passeggeri/merce)
- binario (dove aspettano i passeggeri il treno a.k.a. rotaia della stazione)
- rotaie (normali, ad alta velocità) 
  - velocità max supportata
  - lunghezza
- punti di scambio (in = out) 
  - rotaie in/out
  - (valutare se tenere path normale<->alta velocità o volere scambi con binari uniformi)
- incroci (come punti di scambio ma si incrociano)
- bivii (come punti di scambio ma in < o > out)
- deposito (binario morto)
- Percorso (sequenza di stazioni/tratte)
  - orario di partenza 
  - lista di tratte
- tratta
    - orario partenza programmato/effettivo
    - orario arrivo programmato/effettivo
    - ritardo 
    - tipo di passaggio nella stazione di arrivo? 

### Glossario
- Stazione: punto di partenza e arrivo di un treno
- Treno: veicolo su rotaia che trasporta persone o merci
- Binario: rotaia all'interno della stazione
- Rotaia: struttura su cui si muove il treno
- Incorcio: punto in cui due o più rotaie s'incrociano (può esserci un punto di scambio)
- Deviatoio/scambio: è un componente meccanico che permette di spostare un treno da una rotaia a un altra.
- Punto di scambio: è un'area più ampia della rete ferroviaria che comprende uno o più deviatoi.
- Bivio*: punto di scambio con due o più rotaie in uscita (e viceversa)
- Deposito: binario morto o capolinea
- Linea: l'infrastruttura fisica costituita da binari, stazioni, segnali e altre strutture che collega due o più località.
- Percorso: itinerario seguito dal treno su una o piu linee ferroviarie
- Tratta: collegamento tra due stazioni

## Funzionali
### Utente
- L'utente deve poter interagire con l'applicazione tramite un'interfaccia 
- L'utente deve poter creare una nuova rete ferroviaria potendo specificare e combinare liberamente le 
varie stazioni e tratte, definendo i treni e lo scheduling(tratte valide + orario di partenza)  
  - tramite file (scritto in dsl? o/+ gui)
  - tramite UI (drag and drop?)
- L'utente deve poter vedere l'esecuzione della simulazione una volta avviata
- L'utente deve poter vedere lo scheduling live di un treno (orari programmati/ effettivi e stazioni fatte e da fare)
- L'utente deve poter vedere le tabelloni partenze/arrivi in una stazione
- L'utente deve poter interrompere (pausa) (rewind e modifica live della configurazione?) la simulazione
- L'utente deve poter vedere le statistiche finali e durante la simulazione 
- L'utente deve poter visualizzare gli errori/avvsisi della simulazioni/interagioni con la gui
### Sistema
- Il sistema deve poter visualizzate la rete
- Il sistema deve poter visualizzare le infomazioni delle entità della simulazione
- Il sistema deve poter gestire la crezione di reti ferroviarie
- Il sistema deve poter gestire l'esecuzione della simulazione 
  - (start/stop vari)
  - evitando la sovrapposizione dei treni in una rotaia
  - gestendo la creazione di ritardi 
- Il sistema deve poter gestire la creazione e aggiornamento dei vari scheduling a partire dalle tratte 
- Il sistema deve poter gestire la creazione e aggiornamento dei tabelloni di partenze e arrivi
- Il sistema deve poter gestire il calcolo delle statistiche relative alla simulazione
- Il sistema deve poter controllare la correttezza del setup della simulazione
- Il sistema deve poter replicare una stessa simulazione (valutare random e seed)
- Il sistema deve poter gestire errori e avvisi

## Non funzionali
- Usabilità: l'interfaccia grafica deve essere semplice e intuitiva. 
- Robustezza: l'applicazione deve gestire setup errati
- Affidabilià: l'applicazione deve essere stabile, evitando crash.
- Testabilità: devono essere presenti test per verificare il corretto funzionamento del sistema 
(testare i singoli elementi/ interazioni/ sistema)
- Manutenibilità(?): il codice deve essere ben strutturato e facilmente manutenibile.
- Comprensibilità: l'intero progetto deve essere ben documentato, in modo da facilitare la comprensione del codice.
- Estendibilità: il progetto deve favorire la personalizzazione e l'aggiunta di funzionalità
- Portabilità: l'applicazione può essere eseguita su più sistemi operativi

## Implementazione
- Scala 3.3.4
- ScalaTest 3.2.18
- JDK 17+
- Prolog v.v.v
- ScalaSwing v.v.v
- Cucumber 8.25.1
- TDD (BDD vedendo quando abbiamo usato cucumber?)
- DSL
- SBT as automation tool 1.10.1
- SBT assembly (FAT jar) 2.3.0 
- Scaladoc
- Scalafmt 2.5.2
- archUnit 1.2.0
- wartremover 3.2.5
- semantic release & conventional commit/branch
- Scoverage 2.2.2
- Preferenza per stutture dati immutabili (Full immutabile?)
- Mixin? Gli svilupattori intendono utilizzare i mixin per realizzare quello che normalmente viene implementato con il pattern Component, in quanto sembra essere un buon campo di applicazione di questo concetto. ???

## Opzionali
- scelta tipologia di alimentazione del treno  
  - più tipologie di carrozze (merci, specializzate, ecc...) 
  - per aggiungere scenari treno e rotaie con corrente/magnetiche, e check autonomia 
    (es se disel e tratta troppo lunga si deve fermare a fare benza)
- rotaie (a levitazione magnetica o automatizzati per estendebilità)
- ottimizzazione/ricerca di tratta più veloce (
  - aka definire solo le fermate e non tutte le stazioni e ottimizzare i percorsi tra le fermate 
  introducendo meccaniche di balancing del traffico
- strutture speciali (giro della morte, ponti, gallerie)
- flag fermata lunga (oltre fermata breve e passaggio)
- cambiare colore agli elementi della rete ferroviaria a seconda di quanto sono trafficati/far vedere 
dei box con delle statistiche sopra treni/stazioni
- gestione personalizzata di come si intervallano i treni sulla stessa tratta in caso di sovrapposizioni(e.g. priorità varie/un binario non viene mai percorso due volte di fila nello stesso verso)
- gestione di eventi esterni (pioggia, neve, sciopero, ecc...) eventi che influiscono porzoni di rete