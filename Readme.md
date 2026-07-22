# SCALAMON

## Componenti del gruppo:
- Brighi Federico – federico.brighi2@studio.unibo.it
- Paggetti Marco – marco.paggetti@studio.unibo.it
- Pasini Pietro – pietro.pasini4@studio.unibo.it
- Saponaro Mattia – mattia.saponaro2@studio.unibo.it

## Deadline: 
22/07/2026

## Descrizione del progetto:
Scalamon è un sistema che gestisce combattimenti tattici a turni, uno contro uno, in modalità hot-seat, ispirato alle meccaniche del videogioco online Pokemon Showdown. Il sistema consente a due giocatori di costruire inizialmente una squadra composta da 6 Pokemon (oppure di utilizzare una squadra randomica), ciascuna caratterizzata da Pokemon con statistiche, tipo, abilità e set di mosse proprie. Una volta completata la fase di preparazione, i due giocatori si affrontano in una battaglia suddivisa in turni. Durante ciascun turno, entrambi i giocatori selezionano un’azione tra quelle disponibili (utilizzo di una mossa, sostituzione del personaggio attivo). Una volta effettuate le scelte, il sistema determina l’ordine di esecuzione delle azioni, in base a diversi fattori (statistiche di velocità, condizioni atmosferiche dell’ambiente di gioco, priorità delle mosse, abilità speciali dei Pokemon ed eventuali stati alterati degli stessi). Successivamente, vengono eseguiti il calcolo dei danni, l’applicazione degli effetti secondari, l’aggiornamento degli stati alterati e la gestione di eventuali KO (con sostituzioni forzate). La battaglia si conclude quando uno dei due giocatori esaurisce tutti i Pokemon a disposizione nella propria squadra (non possiedono più punti vita, oppure non hanno più mosse a disposizione).

## Come eseguire:
1) Scaricare il file `PPS25-Scalamon-assembly-0.1.0.jar` dalla sezione `releases`.
2) Eseguire il comando `java -jar PPS25-Scalamon-assembly-0.1.0.jar`
