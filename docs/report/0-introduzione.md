# Ulisse
![Logo generato con ai](/resources/logo.png)

## Obiettivo
L'obiettivo di questo progetto riguarda lo sviluppo di un simulatore Agent-Based per la simulazione di un sistema ferroviario.
Nel simulatore è possibile definire:
 - la struttura della rete ferroviaria, composta da stazioni e tratte
 - i treni disponibili nel sistema
 - gli orari di partenza dei treni

Infine si può avviare la simulazione a partire dal sistema definito potendo:
 - visualizzare l'evoluzione del sistema nel tempo 
 - valutare le prestazioni del sistema attraverso l'analisi delle statistiche prodotte.

Un'ulteriore sfida riguarda la scelta di implementare il sistema in modo immutabile, 
sfuttando ove possibile i vantaggi dei costrutti funzionali di Scala (e.g. Type Class, Extension methods, Mixins, Parametri contestuali), 
riducendo al minimo i side-effect implementando anche una gestione degli errori basata su Option/Either e Validated piuttosto che su eccezioni.
## DEMO


## Autori
- [Federico Bravetti](https://github.com/Fede802)
- [Manuel Buizo](https://github.com/JBmanu)
- [Matteo Violani](https://github.com/TeoV00)

# [Regole d'esame](https://virtuale.unibo.it/mod/page/view.php?route=1295910)
# Note aggiuntive su come preparare la relazione
1. Non si faccia mancare all'inizio una descrizione anche sommaria di cosa il sistema implementato realizza, con alcuni indicazioni  su quali caratteristiche potranno decretare se il progetto ha prodotto un buon risultato.
2. Vista la mole di lavoro dietro al progetto, difficile pensare che i requirement occupino meno di 5-6 facciate: 
   - siano più sistematici possibile, e quindi fungano da specifica completa (si noti che ogni elemento -- statico/strutturale o dinamico/comportamentale -- di dominio va discusso nei requisiti). 
4. Le scelte tecnologiche non dovrebbero essere anticipate troppo per ovvi motivi: prima le prendete prima impattano tutta la parte successiva e quindi diventano più difficilmente riconsiderabili (comunque in linea di principio ogni scelta ha una sua posizione logica precisa, e potrebbe essere nei requirement, nel design o nell'implementazione, a voi la scelta).
5. Attenzione in particolare ai requirement non funzionali: 1) non siano troppo vaghi altrimenti sono inverificabili, e quindi praticamente inutili; 2) se il sistema è distribuito, è inevitable dire esattamente cosa vi aspettate (in retrospettiva, cosa ottenete) in termini di di robustezza a cambiamenti/guasti (quali?, come?), e scalabilità (in quale dimensione? fino a che punto?).
6. Ricordate che una scelta architetturale può ritenersi giustificata o meno solo a fronte dei requirement che avete indicato; viceversa, ogni requirement "critico" dovrebbe influenzare qualcuna della scelte architetturali effettuate e descritte.
7. L'architettura (diagramma + spiegazione in prosa) deve spiegare quali sono i sotto-componenti del sistema (da 5 a 15, diciamo), ognuno cosa fa, chi interagisce con chi e scambiandosi quali dati -- i diagrammi aiutano, ma poi la prosa deve chiaramente indicare questi aspetti.
8. Il design di dettaglio "esplode" (dettaglia) l'architettura, ma viene concettualmente prima dell'implementazione, quindi non metteteci diagrammi ultra-dettagliati estratti dal codice, quelli vanno nella parte di implementazione eventualmente.
9. L'implementazione "esplode" il design, ma solo laddove pensiate che serva dire qualcosa.
10. Cercate di dare una idea di quanto pensate che i vostri test automatizzati coprano il codice e dove: è importante per stimare il potenziale impatto di una modifica al software.
11. Ricordatevi che la lettura della relazione fino a: 
    1. i requirement, deve essere sufficiente per uno sviluppatore per giungere ad un sistema che fa quello che fa il vostro; 
    2. al design, deve essere sufficiente per uno sviluppatore per giungere ad un sistema che in più è organizzato come il vostro; 
    3. alla implementazione, è essenzialmente equivalente al vostro.