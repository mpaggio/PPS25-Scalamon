# Processo di sviluppo 

## Suddivisione in Sprint e ruoli del Team:
All'inizio del progetto, il team ha deciso di adottare un approccio di sviluppo iterativo e incrementale, suddividendo il lavoro in 4 sprint settimanali da circa 15 ore ognuno, per raggiungere il totale complessivo di 60 ore proposto dal corso.
Di comune accordo con il team, sono stati scelti Marco Paggetti come Domain Expert e responsabile del progetto, data la sua maggiore conoscenza del dominio del gioco, e Federico Brighi come Product Owner, con il compito di organizzare sprint e riunioni, garantendo che il team segua le pratiche Agile.

## Divisione in itinere dei Task:
Nel primo meeting di pianificazione, il team si è occupato di redigere il Product Backlog e di definire l'architettura di base del progetto: sono stati definiti X task (consultabili all'interno del file "productbacklog.txt"), suddivisi per i 4 sprint, rappresentanti le funzionalità principali che sarebbero dovute essere presenti nel programma, con priorità e stime di tempo per ciascuno di essi.
Sempre durante il primo meeting il Product Owner ha proposto al team una divisione dei task in base alle competenze e alle preferenze dei membri del team, in modo da massimizzare l'efficienza e la qualità del lavoro svolto.
Queste sono state per lo più rispettate, con alcune modifiche introdotte verso gli ultimi sprint: tali variazioni sono state motivate principalmente dallo stato di avanzamento del progetto, che ha portato il team a decidere di non implementare alcune funzionalità inizialmente descritte come opzionali, per concentrare gli sforzi sul consolidamento e sulla qualità di quelle obbligatorie; in secondo luogo, sono state dovute a specifiche esigenze o problematiche personali dei membri del team.

## Meeting e interazioni pianificate:
Come da prassi Agile / Scrum ognuno dei 4 Sprint è inziato con una riunione di pianificazione (Sprint Planning) in cui, con tutti i membri presenti, sono stati definiti gli obiettivi e le priorità dei task da completare.
Durante lo sprint, i membri del team si sono incontrati quotidianamente per brevi stand-up meeting (Daily Scrum) prima di lavorare al progetto, e 
alla fine di ogni sprint, il team ha tenuto una riunione di revisione (Sprint Review & Retrospective) per valutare i progressi fatti, discutere eventuali problemi incontrati e pianificare miglioramenti per il prossimo sprint, cercando sempre di portare un risultato visibile al cliente.

## Revisione in itinere dei task:
Durante gli sprint, ogni membro del team sviluppava autonomamente la propria parte di competenza, interfacciandosi direttamente con gli altri componenti per la gestione delle integrazioni e di eventuali parti di codice condivise, facendo push delle modifiche direttamente sul branch develop tramite merge, senza l'adozione di un processo formale di Pull Request.
La revisione del lavoro svolto è avvenuta quindi collettivamente a fine sprint, durante la Sprint Review, dove il team verificava insieme il codice integrato e i risultati raggiunti.
Per scelta del team si è lavorato principalmente su un branch separato (develop), e solo al termine dell' ultimo sprint, dopo aver completato tutti i task, il team ha effettuato il merge sul branch principale (main) per la consegna finale del progetto.

## Strumenti di Test, Build e Continuous Integration:
Tra gli strumenti utilizzati durante il progetto, il team ha scelto di utilizzare GitHub per la gestione del codice sorgente e la collaborazione tra i membri del team. Per il testing, sono stati utilizzati strumenti come ScalaTest (AnyFunSuite, Matchers) per garantire la qualità del codice, ScalaFix e Wartremover per il linting e l'individuazione automatica di pattern di codice pericolosi, e SBT come build system per la gestione delle dipendenze e la compilazione del progetto.
Per la documentazione del codice, il team ha utilizzato ScalaDoc, che permette di generare automaticamente la documentazione a partire dai commenti presenti nel codice sorgente.

Non è stato implementato un sistema di Continuous Integration, ma il team ha comunque seguito una strategia di build e test manuale per garantire la qualità del codice, controllando sempre di avere codice funzionante, testato (con una coverage minima del 50%, verificata lanciando il comando "sbt coverage test coverageReport" e ispezionando il file index.html generato) e documentato tramite ScalaDoc.
