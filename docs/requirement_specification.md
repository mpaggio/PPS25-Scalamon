# Specifica dei requisiti

## 1) Requisiti di business:

### BR-01: Simulazione di combattimenti tattici:
Il sistema deve permettere a due giocatori di affrontarsi in combattimenti Pokémon uno contro uno, caratterizzati da una componente strategica significativa. La battaglia deve essere basata sulla scelta delle azioni effettuate durante ogni turno e non sulla semplice esecuzione automatica degli attacchi. Il giocatore deve poter prendere decisioni tenendo conto di diversi fattori, tra cui la composizione della squadra, la scelta delle mosse disponibili, il consumo delle risorse, i punti di forza e debolezza dei diversi Pokémon e delle diverse mosse, le condizioni ambientali presenti durante lo scontro e gli eventuali stati alterati applicati ai Pokémon. Il sistema deve inoltre garantire un'esperienza dove la vittoria dipenda dalla capacità del giocatore di pianificare le proprie mosse e adattarsi alle situazioni generate durante la partita.

### BR-02: Personalizzazione della squadra:
Il sistema deve consentire ai giocatori di costruire la propria squadra prima dell'inizio della battaglia. Ogni giocatore deve poter disporre di una squadra composta da 6 Pokémon differenti, evitando quindi duplicati all'interno della stessa squadra. La creazione deve poter avvenire attraverso modalità differenti, permettendo sia un inizio rapido, sia una scelta più approfondita, adattandosi così agli utenti interessati solamente alla simulazione e a coloro che vogliono strategie specifiche.
- La prima modalità deve consentire una generazione automatica della squadra, utile per iniziare rapidamente una partita, senza dover configurare manualmente ogni elemento.
- La seconda modalità deve permettere al giocatore una personalizzazione completa, dando la possibilità di scegliere direttamente i Pokémon e le relative mosse.

### BR-03: Esperienza di gioco in modalità hot-seat:
Il sistema deve supportare una modalità multiplayer locale, nella quale due utenti utilizzano lo stesso dispositivo, senza necessità di una connessione di rete. Durante la fase di selezione delle azioni, il sistema deve garantire che ogni giocatore possa effettuare le proprie decisioni, venendo avvisato del cambio del turno di scelta, così da garantire la segretezza di tale azione. La modalità hot-seat prevede che entrambi gli utenti condividano lo stesso schermo, ma che mantengano comunque segrete le proprie scelte. Il sistema deve quindi gestire correttamente il passaggio del controllo tra i due giocatori, mostrando un'apposita indicazione quando è necessario cambiare utente.

### BR-04: Riproduzione coerente delle regole di combattimento:
Il sistema deve garantire che la simulazione della battaglia produca risultati coerenti con le regole definite dal dominio implementato. Ogni turno deve essere risolto seguendo un insieme preciso di regole che determinano l'ordine delle azioni e gli effetti prodotti. La risoluzione deve considerare elementi quali la priorità delle mosse, la velocità dei Pokémon coinvolti, gli effetti dovuti alle condizioni ambientali, gli stati alterati attivi e la gestione delle situazioni di KO. A parità di condizioni iniziali e di risultati casuali generati, il motore di combattimento deve produrre sempre lo stesso risultato.

### BR-05: Architettura estendibile:
Il sistema deve rappresentare un esempio di applicazione pratica dei paradigmi di programmazione funzionale studiati durante il corso. L'architettura deve essere progettata in modo da permettere l'introduzione futura di nuovi elementi del gioco (Pokémon aggiuntivi, nuove mosse, nuovi strumenti, nuove condizioni ambientali, ...) senza richiedere modifiche invasive alle componenti già esistenti.

## 2) Requisiti funzionali:

### 2.1) Gestione del dominio

#### FR-01: Gestione Pokémon
Il sistema deve rappresentare ogni Pokémon attraverso un modello che distingua chiaramente tra informazioni statiche (che definiscono la specie del Pokémon) da quelle dinamiche (che variano durante la battaglia).
- Le informazioni statiche devono comprendere il nome del Pokémon, il tipo o i tipi di appartenenza e le statistiche di base che ne determinano le caratteristiche in combattimento (punti salute o HP, attacco, difesa, attacco speciale, difesa speciale, velocità). Rappresentano le caratteristiche intrinseche (visibili come valori massimi a cui fare riferimento) e non devono essere modificate durante il combattimento.
- Le informazioni dinamiche devono comprendere i punti salute correnti, gli eventuali stati alterati attivi, i modificatori applicati alle statistiche e le mosse disponibili.

#### FR-02: Gestione delle mosse
Ogni Pokémon deve poter possedere un insieme di quattro mosse, utilizzabili durante il combattimento. Le mosse devono essere rappresentate da un modello comune che permetta di gestire sia attacchi offensivi, sia mosse con effetto di supporto o modifica dello stato della battaglia (*mosse status*).
- Le mosse dannose devono contenere tutte le informazioni necessarie per il calcolo del danno, tra cui il tipo della mossa, la potenza base, la categoria dell'attacco (*fisico* o *speciale*), il valore di precisione e il numero massimo di utilizzi disponibili (*PP*). Durante l'esecuzione di una mossa offensiva, il sistema deve calcolare il danno considerando diversi fattori. In particolare devono essere valutate le statistiche dell'attaccante e del difensore in base alla categoria della mossa utilizzata, il bonus (*STAB*, *Same Type Attack Bonus*) nel caso il tipo della mossa coincida con uno dei tipi del Pokémon in gioco e gli eventuali modificatori derivanti da condizioni ambientali, abilità o stati alterati.
- Le mosse di tipo status devono invece permettere di applicare effetti che non causano danno diretto. Attraverso queste mosse deve essere possibile modificare le statistiche, applicare condizioni alterate, modificare le condizioni ambientali oppure generare altri effetti temporanei sul campo di battaglia.

Il sistema deve inoltre impedire l'utilizzo di una mossa quando questa non dispone più di PP disponibili, comunicando opportunamente al giocatore il motivo per cui l'azione non può essere eseguita.

#### FR-03: Gestione degli strumenti consumabili
Il sistema deve fornire ad ogni giocatore un insieme iniziale di strumenti utilizzabili durante la battaglia. Gli strumenti devono essere generati casualmente all'inizio della partita e devono rappresentare una risorsa limitata che il giocatore può utilizzare strategicamente durante lo scontro. Durante il proprio turno, il giocatore deve poter scegliere di utilizzare uno strumento disponibile invece di eseguire una mossa o cambiare Pokémon. Ogni strumento deve avere un effetto specifico sullo stato della partita, come il recupero dei punti salute, la modifica temporanea di statistiche oppure la rimozione di determinati effetti negativi. Dopo essere stato utilizzato, uno strumento deve essere consumato e non deve più essere disponibile nei turni successivi.

#### FR-04: Gestione delle abilità del Pokémon
Il sistema deve supportare abilità associate ai Pokémon, che possono modificare il comportamento standard della battaglia. Le abilità devono poter essere attivate in seguito a specifici eventi del gioco, come l'ingresso in campo di un Pokémon, la ricezione di un danno, la fine del turno oppure il verificarsi di condizioni particolari. Gli effetti prodotti da un'abilità possono riguardare diversi aspetti della battaglia, come la modifica del danno inflitto o ricevuto, la cura dei punti salute, l'applicazione o rimozione di stati alterati, la modifica delle condizioni atmosferiche oppure la creazione di immunità rispetto a determinti effetti.

#### FR-05: Gestione dell'ambiente
All'inizio di ogni partita il sistema deve generare una condizione ambientale iniziale casuale, che rimane condivisa tra entrambi i giocatori. L'ambiente deve rappresentare un elemento strategico della battaglia e deve poter modificare il comportamento di alcune mosse o degli effetti applicati durante il combattimento. Il sistema deve prevedere diverse condizioni atmosferiche, tra cui cielo sereno, sole intenso, pioggia, nebbia e tempesta di fulmini. Ogni condizione ambientale può influenzare diversi aspetti della battaglia, come la potenza delle mosse di determinati tipi, la precisione degli attacchi, la possibilità di applicare alcuni stati alterati, il danno residuo prodotto a fine turno oppure la capacità di alcuni Pokémon di recuperare punti salute. Le condizioni ambientali devono poter essere modificate durante la partita, attraverso specifiche mosse o abilità.
- Influenzare il calcolo del danno.
- Poter esseree modificato da specifiche abilità.
- Essere condiviso da entrambi i giocatori.

### 2.2) Gestione della partita:

#### FR-05: Creazione della squadra
Il sistema deve permettere la creazione di due squadre composte da:
- Esattamente 6 Pokémon.
- Pokémon non duplicati all'interno della stessa squadra.
- 4 mosse per ogni Pokémon.

#### FR-06: Generazione automatica del team
La generazione automatica del team deve poter prevedere due possibili logiche:
- Generazione completamente casuale, che seleziona randomicamente 6 Pokémon dal database e assegna ad ognuno 4 mosse random, senza tenere conto di alcun fattore.
- Generazione affine, che seleziona randomicamente 6 Pokémon dal database e ad ognuna assegna 2 mosse dello stesso tipo e 2 mosse che gli permettono di avere una copertura di attacco su tipi su cui (con le prime 2 mosse) non avrebbe molto effetto.

#### FR-07: Creazione manuale del team
La creazione manuale del team deve consentire:
- Visualizzazione dei Pokémon disponibili.
- Scelta dei Pokémon (uno alla volta) tra quelli disponibili.
- Consultazione delle statistiche.
- Visualizzazione delle mosse disponibili.
- Scelta delle 4 mosse (una alla volta) per ciascun Pokémon tra quelli scelti (preso uno alla volta).

#### FR-08: Gestione del turno
Durante ogni turno, ogni giocatore deve poter scegliere una sola azione tra:
- Utilizzo di una mossa del Pokémon in campo.
- Cambio del Pokémon corrente con uno in panchina.
- Utilizzo di uno strumento.

#### FR-09: Risoluzione automatica del turno
Il motore di combattimento deve risolvere le azioni secondo il seguente ordine:
1) Effetti iniziali.
2) Strumenti / Sostituzioni.
3) Mosse.
4) Effetti di fine turno.
In caso di conflitto, se si tratta di due mosse deve utilizare la velocità del Pokémon come criterio di spareggio, mentre se si tratta di strumento/sostituzione allora li esegue nell'ordine di scelta (prima il giocatore 1 e poi il giocatore 2).

#### FR-10: Gestione degli stati alterati
Il sistema deve supportare i seguenti stati alterati:
- Bruciatura.
- Veleno.
- Paralisi.
- Sonno.
- Congelamento.
Ogni stato può prevedere:
- Modifiche statistiche.
- Limitazioni sulle azioni.
- Danni periodici.

#### FR-11: Gestione KO e fine partita
Il sistema deve rilevare automaticamente il KO di un Pokémon e chiederne l'immediata sostituzione. Il sistema deve anche dichiarare il vincitore quando una squadra perde tutti i Pokémon.

## 3) Requisiti non funzionali:

### NFR-01: Usabilità
L'interfaccia deve permettere agli utenti di comprendere facilmente lo stato della battaglia, selezionare rapidamente le azioni e visualizzare informazioni rilevanti sullo svolgimento della battaglia.

### NFR-02: Affidabilità
Il sistema deve garantire la coerenza dello stato della partita, l'assenza di transizioni impossibili e il corretto aggiornamento dei dati.

### NFR-03: Manutenibilità
Il codice deve essere organizzato in componenti e le logiche principali devono essere separate tra dominio, gestione della logica, motore di combattimento e interfaccia grafica.

### NFR-04: Estendibilità
Il sistema deve permettere future estensioni quali:
- Nuove mosse.
- Nuovi Pokémon.
- Nuovi ambienti.
- Nuovi strumenti.
L'aggiunta di nuovi elementi non deve richiedere modifiche significative al motore principale.

### NFR-05: Performance
Le operazioni durante il combattimento devono essere eseguite in tempo reale. Il sistema deve garantire tempi di risposta ridotti durante la selezione delle azioni, il calcolo del danno e l'aggiornamento dello stato.

## 4) Requisiti di implementazione:

### IR-01: Linguaggio di sviluppo
Il sistema deve essere sviluppato utilizzando il linguaggio Scala 3 come linguaggio principale. In particolare, il progetto deve fare uso delle funzionalità introdotte nelle versioni più recenti del linguaggio, come ad esempio i meccanismi di gestione del contesto, i tipi algebrici e le estensioni dei tipi. La versione specifica del linguaggio Scala deve essere la 3.4.2

### IR-02: Sistema di build
La gestione della compilazione, delle dipendenze e dell'esecuzione del progetto deve essere effettuata attraverso *SBT* (*Scala Build Tool*). La configurazione del progetto deve permettere di compilare il codice sorgente, eseguire l'applicazione principale, eseguire la suite di test automatizzati e gestire le librerie esterne necessarie.

### IR-03: Organizzazione architetturale
L'implementazione deve seguire una struttura modulare nella quale ogni componente del sistema abbia una responsabilità ben definita. Il progetto deve presentare una parte di dominio (rappresentazione delle entità principali del gioco e delle relative regole statiche), una parte di logica applicativa (che si occupa della gestione dello stato della partita e delle operazioni che modificano il comportamento del sistema), un livello applicativo (che si occupa di coordinare il funzionamento generale del programma e collegare la logica di gioco con l'interfaccia) e una parte di interfaccia grafica (responsabile solamente della presentazione delle informazioni all'utente e della raccolta delle azioni effettuate tramite la stessa).

### IR-04: Utilizzo del paradigma funzionale
L'implementazione deve seguire un approccio prevalentemente funzionale, limitando il più possibile la presenza di stato modificabile. In particolare, una scelta effettuata durante un turno deve produrre una nuova configurazione della battaglia anziché modificare direttamente quella precedente.

### IR-05: Gestione dei contenuti di gioco tramite DSL
Le informazioni relative agli elementi del dominio devono essere definite attraverso strutture dichiarative. Il progetto deve utilizzare *DSL* (*Domain Specific Language*) interno per permettere di descrivere i contenuti del gioco in modo leggibile.