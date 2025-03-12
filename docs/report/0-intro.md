# Ulisse
<p align="center">
  <a href="https://github.com/dev-pps/pps-23-ulisse">
    <img src="/resources/logo.png" style="width: 50%">
  </a>
</p>

## Obiettivo
L'obiettivo di questo progetto riguarda lo sviluppo di un simulatore *Agent-Based* per la simulazione di un sistema ferroviario.
Nel simulatore è possibile definire:
 - la struttura della rete ferroviaria, composta da stazioni e tratte
 - i treni disponibili nel sistema
 - gli orari di partenza dei treni

Infine si può avviare la simulazione a partire dal sistema definito potendo:
 - visualizzare l'evoluzione del sistema nel tempo 
 - valutare le prestazioni del sistema attraverso l'analisi delle statistiche prodotte

Un'ulteriore sfida riguarda la scelta di implementare il sistema in modo immutabile, 
sfuttando ove possibile i vantaggi dei costrutti funzionali di Scala (e.g. Type Class, Extension method, Mixin, Parametri contestuali), 
riducendo al minimo i side-effect implementando anche una gestione degli errori basata su Option/Either e Validated piuttosto che su eccezioni.
## Demo
<div style="display: flex; justify-content: center; align-items: center; height: 100%;">
  <video width="640" height="480" loop autoplay>
    <source src="/resources/demo/demo.mp4" type="video/mp4">
  </video>
</div>

## Autori
- [Federico Bravetti](https://github.com/Fede802)
- [Manuel Buizo](https://github.com/JBmanu)
- [Matteo Violani](https://github.com/TeoV00)