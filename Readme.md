# SCALAMON

## Componenti del gruppo:
- Brighi Federico – federico.brighi2@studio.unibo.it
- Paggetti Marco – marco.paggetti@studio.unibo.it
- Pasini Pietro – pietro.pasini4@studio.unibo.it
- Saponaro Mattia – mattia.saponaro2@studio.unibo.it

## Deadline: 
22/07/2026

## Processo di sviluppo: 
Il gruppo intende adottare il processo di sviluppo consigliato nel punto P8 delle regole d’esame.

## Divisione del lavoro: 
Nel primo meet verranno definiti i requisiti del sistema che successivamente ad ogni sprint planning saranno analizzati e suddivisi in task. L'assegnamento di quest'ultimi verrà deciso secondo la metodologia AGILE/SCRUM.

## Descrizione del progetto:
Scalamon è un sistema che gestisce combattimenti tattici a turni, uno contro uno, in modalità hot-seat, ispirato alle meccaniche del videogioco online Pokemon Showdown. Il sistema consente a due giocatori di costruire inizialmente una squadra composta da 6 Pokemon (oppure di utilizzare una squadra randomica), ciascuna caratterizzata da Pokemon con statistiche, tipo, abilità e set di mosse proprie. Una volta completata la fase di preparazione, i due giocatori si affrontano in una battaglia suddivisa in turni. Durante ciascun turno, entrambi i giocatori selezionano un’azione tra quelle disponibili (utilizzo di una mossa, sostituzione del personaggio attivo). Una volta effettuate le scelte, il sistema determina l’ordine di esecuzione delle azioni, in base a diversi fattori (statistiche di velocità, condizioni atmosferiche dell’ambiente di gioco, priorità delle mosse, abilità speciali dei Pokemon ed eventuali stati alterati degli stessi). Successivamente, vengono eseguiti il calcolo dei danni, l’applicazione degli effetti secondari, l’aggiornamento degli stati alterati e la gestione di eventuali KO (con sostituzioni forzate). La battaglia si conclude quando uno dei due giocatori esaurisce tutti i Pokemon a disposizione nella propria squadra (non possiedono più punti vita, oppure non hanno più mosse a disposizione).

## Funzionalità obbligatorie:
- **Motore di battaglia**: sistema principale, responsabile della gestione completa del combattimento. Comprende il calcolo del danno, la gestione dell’ordine dei turni, l’utilizzo delle mosse, gestione della sconfitta di un Pokemon, eventuali sostituzioni (volontarie o forzate) e verifica delle condizioni di vittoria e sconfitta.
- **Sistema dei turni**: gestione completa del flusso di gioco. Comprende la selezione delle azioni da parte dei giocatori, la determinazione dell’ordine di esecuzione, esecuzione delle azioni, aggiornamento dello stato della battaglia, generazione del log testuale e passaggio al turno successivo.
- **Elementi del dominio**:
    - *Pokemon*: entità principale, caratterizzati da varie statistiche, come i punti salute (HP), statistiche offensive e difensive, tipo, velocità, insieme di mosse disponibili e stati alterati.
    - *Mosse*: azioni utilizzabili durante la battaglia, caratterizzate da tipo, danno, precisione, numero massimo di utilizzi (PP) ed eventuali effetti secondari.
    - *Stati alterati*: sistema di effetti persistenti, applicabili ai Pokemon, come veleno, bruciatura, paralisi, sonno, congelamento, con effetti ad inizio e/o a fine del turno. Tali stati possono influenzare statistiche, possibilità di azione e perdita di salute durante il turno.
- **Team-builder**: sistema di preparazione della squadra, che consente ai giocatori di costruire una squadra in maniera randomica, oppure che di selezionare i Pokemon che si desiderano all’interno della squadra, assegnare casualmente mosse compatibili col tipo del Pokemon e visualizzare statistiche e caratteristiche principali dei Pokemon selezionati.
- **Interfaccia utente**: interfaccia grafica minimale.

## Funzionalità opzionali:
- **Sistema di strumenti**: uno strumento trasforma lo stato del gioco (cura HP, resuscita un membro della squadra, aumenta le statistiche, …).
- **Sistema di obiettivi**: vittoria senza perdite di membri della squadra (etc…).
- **Sistema di accessori**: a ciascun Pokemon può essere associato un accessorio che ne altera le statistiche (accessorio che aumenta la difesa, che aumenta gli attacchi di un tipo specifico, …).
- **Possibilità di giocare contro il computer**: il computer agisce tramite ricerca esaustiva o tramite euristiche.