# Testing
Per quanto riguarda il testing si è cercato di adottare il più possibile l'approccio TDD nello svilupoo dei sorgenti. L'utilizzo di questa metodologia ha permesso di prevenire la comparsa di comportamenti inattesi/indesiderati.

Sono state utilizzate per lo sviluppo dei test le seguenti tecnologie: 
- mockito (scalatestplus): dove si utilizzano oggetti fittizi (mock) per simulare il comportamento di componenti reali del sistema. Questo permette di isolare il codice da dipendenze esterne e di testare in modo più controllato e prevedibile.
- scalatest: è stato adottato per garantire un approccio idiomatico nello sviluppo dei test in Scala, sfruttando i diversi stili di scrittura dei test e integrandosi in modo naturale con il linguaggio.
- archunit:impiegato per validare la conformità alle regole dell'architettura esagonale, garantendo il rispetto delle dipendenze tra i layer e imponendo vincoli strutturali sui nomi e sulla collocazione delle classi nei package.

## Scoverage
Per ottenere risultati più accurati e rappresentativi sono state escluse dal calcolo della coverage i sorgenti presenti nel package `view`.
Di seguito vengono mostrati i risultati di coverage ottenuti.
<p align="center">
    <iframe src="https://dev-pps.github.io/pps-23-ulisse/coverage/index.html" width="800" height="600"></iframe>
</p>

## Considerazioni
L'adozione del Test-Driven Development (TDD) ha garantito un processo di sviluppo controllato, prevenendo l'introduzione di comportamenti anomali e facilitando attività di refactoring in totale sicurezza grazie all'affidabilità della suite di test.
Questa strategia ha inoltre semplificato e velocizzato l'integrazione dei componenti sviluppati in parallelo da diversi membri del team, assicurando l'allineamento tra le interfacce esposte e riducendo il rischio di incongruenze architetturali. La copertura estesa dei test ha reso trasparente la verifica del corretto funzionamento dei moduli integrati, migliorando la qualità complessiva del sistema.