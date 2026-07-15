# Processo di sviluppo 

## Suddivisione in Sprint e ruoli del Team:
All'inizio del progetto, il team ha deciso di adottare un approccio di sviluppo iterativo e incrementale, basato sulla metodologia di sviluppo Agile, in particolare adottando la variante Scrum. Il lavoro è stato suddiviso in 4 sprint settimanali da circa 15 ore ognuno, per raggiungere il totale complessivo di 60 ore proposto dal corso. Il team è stato organizzato nel seguente modo:
- Domain Expert (Marco Paggetti): responsabile del progetto ed esperto del dominio di gioco.
- Product Owner (Federico Brighi): responsabile dell'organizzazione degli sprint e garante dell'utilizzo della metodologia Scrum da parte del team.

## Divisione in itinere dei Task:
Nel primo meeting di pianificazione, il team si è occupato di redigere il Product Backlog e di definire l'architettura di base del progetto: sono stati definiti X task (consultabili all'interno del file "productbacklog.txt"), suddivisi per i 4 sprint, rappresentanti le funzionalità principali che sarebbero dovute essere presenti nel programma, con priorità e stime di tempo per ciascuno di essi. Sempre durante il primo meeting il Product Owner ha proposto al team una divisione dei task in base alle competenze e alle preferenze dei membri del team, in modo da massimizzare l'efficienza e la qualità del lavoro svolto. Queste sono state per lo più rispettate, con alcune modifiche introdotte verso gli ultimi sprint: tali variazioni sono state motivate principalmente dallo stato di avanzamento del progetto, che ha portato il team a decidere di non implementare alcune funzionalità inizialmente descritte come opzionali, per concentrare gli sforzi sul consolidamento e sulla qualità di quelle obbligatorie; in secondo luogo, sono state dovute a specifiche esigenze o problematiche personali dei membri del team.

## Meeting e interazioni pianificate:
Come da prassi Scrum, ognuno dei 4 Sprint è iniziato con una riunione di pianificazione (Sprint Planning) in cui, con tutti i membri presenti, sono stati definiti gli obiettivi e le priorità dei task da completare. Durante lo sprint, i membri del team si sono incontrati quotidianamente per brevi stand-up meeting (Daily Scrum) prima di lavorare al progetto, e alla fine di ogni sprint, il team ha tenuto una riunione di revisione (Sprint Review & Retrospective) per valutare i progressi fatti, discutere eventuali problemi incontrati e pianificare miglioramenti per lo sprint successivo, cercando sempre di portare un risultato visibile allo stakeholder.

## Revisione in itinere dei task:
Durante gli sprint, ogni membro del team ha sviluppato autonomamente la propria parte di competenza, interfacciandosi direttamente con gli altri componenti per la gestione delle integrazioni e di eventuali parti di codice condivise, facendo push delle modifiche direttamente sul branch develop tramite merge, senza l'adozione di un processo formale di Pull Request. La revisione del lavoro svolto è avvenuta quindi collettivamente a fine sprint, durante la Sprint Review, dove il team ha verificato insieme il codice integrato e i risultati raggiunti. Per scelta del team si è lavorato principalmente su un branch separato (develop), e al termine del secondo e dell' ultimo sprint, dopo aver completato tutti i task relativi, il team ha effettuato il merge sul branch principale (main) per la consegna del risultato parziale/finale funzionante del progetto.

## Strumenti di Test, Build e Continuous Integration:
Tra gli strumenti utilizzati durante il progetto, il team ha scelto di utilizzare GitHub per la gestione del codice sorgente e la collaborazione tra i membri del team. Per il testing, sono stati utilizzati strumenti come ScalaTest (AnyFunSuite, Matchers) per garantire la qualità del codice (Quality Assurance), ScalaFix e Wartremover per il linting e l'individuazione automatica di pattern di codice pericolosi, e SBT come build system per la gestione delle dipendenze e la compilazione del progetto. Per la documentazione del codice, il team ha utilizzato ScalaDoc, che permette di generare automaticamente la documentazione a partire dai commenti presenti nel codice sorgente.

Non è stato implementato un sistema di Continuous Integration, ma il team ha comunque seguito una strategia di build e test manuale per garantire la qualità del codice, controllando sempre di avere codice funzionante, testato (con una coverage minima del 50%, verificata lanciando il comando "sbt coverage test coverageReport" e ispezionando il file index.html generato) e documentato tramite ScalaDoc.









## Product Backlog

| Priority | ID   | Item                                                                                          | Initial size estimate |
|----------|------|-----------------------------------------------------------------------------------------------|-----------------------|
| 1        | PB01 | Come giocatore, devo poter creare una squadra manualmente                                     | 8                     |
| 2        | PB02 | Come giocatore, devo poter generare casualmente una squadra                                   | 5                     |
| 3        | PB03 | Come giocatore, voglio che ogni Pokémon abbia statistiche, tipi e abilità proprie             | 13                    |
| 4        | PB04 | Come giocatore, voglio che i Pokémon utilizzino mosse che abbiano PP e accuracy               | 13                    |
| 5        | PB05 | Come giocatore, voglio che il danno venga gestito correttamente con tutte le sue dipendenze.  | 13                    |
| 6        | PB06 | Come giocatore, voglio che l'ordine del turno sia risolto automaticamente                     | 8                     |
| 7        | PB07 | Come giocatore, voglio che lo status dei Pokémon possa essere alterato                        | 13                    |
| 8        | PB08 | Come giocatore, voglio che le condizioni meteo influiscano sulla battaglia                    | 8                     |
| 9        | PB09 | Come giocatore, voglio che un Pokémon KO venga automaticamente scambiato                      | 5                     |
| 10       | PB10 | Come giocatore, devo poter visualizzare i log della battaglia                                 | 5                     |
| 11       | PB11 | Come giocatore, devo avere condizioni di vittoria e di sconfitta                              | 3                     |
| 12       | PB12 | Come giocatore, voglio avere un loop completo della battaglia                                 | 13                    |
| 13       | PB13 | Come giocatore, voglio che le abilità dei Pokémon influenzino la battaglia in maniera passiva | 13                    |
| 14       | PB14 | Come giocatore, voglio che gli strumenti modifichino le statistiche o le mosse dei Pokémon    | 8                     |
| 15       | PB15 | Come giocatore, voglio poter interagire con un interfaccia grafica                            | 13                    |

---

## Sprint 1 

| Product Backlog Item | Sprint Task                                                                          | Volunteer |
|----------------------|--------------------------------------------------------------------------------------|-----------|
| PB03                 | Definire il modello Pokémon (Pokemon, Stats, AbilitySlot) e DSL del Pokédex          | Brighi    |
| PB04, PB05           | Definire il modello delle mosse (DamageMove, StatusMove) solo dati, senza esecuzione | Paggetti  |
| PB05                 | Definire le relazioni tra i diversi tipi di Pokemon                                  | Saponaro  |
| PB03, PB04, PB05     | Stato dinamico delle componenti di dominio                                           | Pasini    |

---

## Sprint 2 

| Product Backlog Item   | Sprint Task                                                                                                                                                           | Volunteer |
|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| PB04, PB05, PB06, PB10 | Definire l'ordine delle azioni (velocità + priorità) e struttura del flusso del turno (inizio, ordinamento, passaggio all'esecuzione, no fine) + log basici di turno  | Saponaro  |
| PB09, PB10, PB11       | Verifica KO, vittoria/sconfitta, switch forzati e fine turno. Definire formula del calcolo del danno  conmodificatore per livello di difficoltà + log basici di turno | Brighi    |
| PB04, PB05             | Implementare tutto ciò che consegue l'utilizzo di una mossa (consumazione PP + esecuzione della mossa e relative conseguenze)                                         | Paggetti  |
| PB04, PB06, PB14       | Inizializzare il sistema di strumenti e gestire le interfacce comuni condivise dai vari componenti del dominio                                                        | Pasini    |

---

## Sprint 3

| Product Backlog Item | Sprint Task                                                                                                                                 | Volunteer |
|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| PB13, PB05           | Implementare abilità passive (Ability) con DSL, eseguire nel turn resolution gli effetti di fine turno relativi a abilità, status e weather | Brighi    |
| PB07                 | Implementare stati alterati e loro controlli e conseguenze                                                                                  | Paggetti  |
| PB08, PB05           | Definire sistema meteo e relativi modificatori, integrarando i suoi effetti anche nel calcolo del danno (con Brighi)                        | Saponaro  |
| PB14                 | Implementare strumenti e relativi effetti su statistiche e mosse                                                                            | Pasini    |

---

## Sprint 4

| Product Backlog Item | Sprint Task                                                                                | Volunteer |
|----------------------|--------------------------------------------------------------------------------------------|-----------|
| PB01, PB12, PB15     | Implementare la creazione manuale della squadra (selezione Pokémon + mosse) + sviluppo GUI | Brighi    |
| PB02, PB12, PB15     | Implementare la generazione casuale/affine della squadra + sviluppo GUI                    | Paggetti  |
| PB10, PB12, PB15     | Gestione del Logger cronologico di tutti gli eventi della battaglia                        | Saponaro  |
| PB10, PB12, PB06     | Gestione del Logger + sviluppo GUI + refactor e riorganizzazione MVC del progetto          | Pasini    |