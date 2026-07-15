# Specifica dei requisiti

## 1) Requisiti di business:

### BR-01) Simulazione di combattimenti tattici:
Il sistema deve permettere a due giocatori di affrontarsi in combattimenti Pokémon uno contro uno, caratterizzati da una componente strategica significativa. La battaglia deve essere basata sulla scelta delle azioni effettuate durante ogni turno e non sulla semplice esecuzione automatica degli attacchi. Il giocatore deve poter prendere decisioni tenendo conto di diversi fattori, tra cui la composizione della squadra, la scelta delle mosse disponibili, il consumo delle risorse, i punti di forza e debolezza dei diversi Pokémon e delle diverse mosse, le condizioni ambientali presenti durante lo scontro e gli eventuali stati alterati applicati ai Pokémon. Il sistema deve inoltre garantire un'esperienza dove la vittoria dipenda dalla capacità del giocatore di pianificare le proprie mosse e adattarsi alle situazioni generate durante la partita.

### BR-02) Personalizzazione della squadra:
Il sistema deve consentire ai giocatori di costruire la propria squadra prima dell'inizio della battaglia. Ogni giocatore deve poter disporre di una squadra composta da 6 Pokémon differenti, evitando quindi duplicati all'interno della stessa squadra. La creazione deve poter avvenire attraverso modalità differenti, permettendo sia un inizio rapido, sia una scelta più approfondita, adattandosi così agli utenti interessati solamente alla simulazione e a coloro che vogliono strategie specifiche.
- La prima modalità deve consentire una generazione automatica della squadra, utile per iniziare rapidamente una partita, senza dover configurare manualmente ogni elemento.
- La seconda modalità deve permettere al giocatore una personalizzazione completa, dando la possibilità di scegliere direttamente i Pokémon e le relative mosse.

### BR-03) Esperienza di gioco in modalità hot-seat:
Il sistema deve supportare una modalità multiplayer locale, nella quale due utenti utilizzano lo stesso dispositivo, senza necessità di una connessione di rete. Durante la fase di selezione delle azioni, il sistema deve garantire che ogni giocatore possa effettuare le proprie decisioni, venendo avvisato del cambio del turno di scelta, così da garantire la segretezza di tale azione. La modalità hot-seat prevede che entrambi gli utenti condividano lo stesso schermo, ma che mantengano comunque segrete le proprie scelte. Il sistema deve quindi gestire correttamente il passaggio del controllo tra i due giocatori, mostrando un'apposita indicazione quando è necessario cambiare utente.

### BR-04) Riproduzione coerente delle regole di combattimento:
Il sistema deve garantire che la simulazione della battaglia produca risultati coerenti con le regole definite dal dominio implementato. Ogni turno deve essere risolto seguendo un insieme preciso di regole che determinano l'ordine delle azioni e gli effetti prodotti. La risoluzione deve considerare elementi quali la priorità delle mosse, la velocità dei Pokémon coinvolti, gli effetti dovuti alle condizioni ambientali, gli stati alterati attivi e la gestione delle situazioni di KO. A parità di condizioni iniziali e di risultati casuali generati, il motore di combattimento deve produrre sempre lo stesso risultato.

## 2) Requisiti funzionali:

### 2.1) Gestione del dominio

#### FR-01) Gestione Pokémon
Il sistema deve rappresentare ogni Pokémon attraverso un modello che distingua chiaramente tra informazioni statiche (che definiscono la specie del Pokémon) da quelle dinamiche (che variano durante la battaglia).
- Le informazioni statiche devono comprendere il nome del Pokémon, il tipo o i tipi di appartenenza e le statistiche di base che ne determinano le caratteristiche in combattimento (punti salute o HP, attacco, difesa, attacco speciale, difesa speciale, velocità). Rappresentano le caratteristiche intrinseche (visibili come valori massimi a cui fare riferimento) e non devono essere modificate durante il combattimento.
- Le informazioni dinamiche devono comprendere i punti salute correnti, gli eventuali stati alterati attivi, i modificatori applicati alle statistiche e le mosse disponibili.

#### FR-02) Gestione delle mosse
Ogni Pokémon deve poter possedere un insieme di quattro mosse, utilizzabili durante il combattimento. Le mosse devono essere rappresentate da un modello comune che permetta di gestire sia attacchi offensivi, sia mosse con effetto di supporto o modifica dello stato della battaglia (*mosse status*).
- Le mosse dannose devono contenere tutte le informazioni necessarie per il calcolo del danno, tra cui il tipo della mossa, la potenza base, la categoria dell'attacco (*fisico* o *speciale*), il valore di precisione e il numero massimo di utilizzi disponibili (*PP*). Durante l'esecuzione di una mossa offensiva, il sistema deve calcolare il danno considerando diversi fattori. In particolare devono essere valutate le statistiche dell'attaccante e del difensore in base alla categoria della mossa utilizzata, il bonus (*STAB*, *Same Type Attack Bonus*) nel caso il tipo della mossa coincida con uno dei tipi del Pokémon in gioco e gli eventuali modificatori derivanti da condizioni ambientali, abilità o stati alterati.
- Le mosse di tipo status devono invece permettere di applicare effetti che non causano danno diretto. Attraverso queste mosse deve essere possibile modificare le statistiche, applicare condizioni alterate, modificare le condizioni ambientali oppure generare altri effetti temporanei sul campo di battaglia.

Il sistema deve considerare la possibilità che una mossa fallisca sulla base del proprio valore di precisione. Inoltre, il sistema deve impedire l'utilizzo di una mossa quando questa non dispone più di PP disponibili, comunicando opportunamente al giocatore il motivo per cui l'azione non può essere eseguita.

#### FR-03) Gestione degli strumenti consumabili
Il sistema deve fornire a ogni giocatore un insieme iniziale di strumenti utilizzabili durante la battaglia. Gli strumenti devono essere generati casualmente all'inizio della partita e devono rappresentare una risorsa limitata che il giocatore può utilizzare strategicamente durante lo scontro. Durante il proprio turno, il giocatore deve poter scegliere di utilizzare uno strumento disponibile invece di eseguire una mossa o cambiare Pokémon. Ogni strumento deve avere un effetto specifico sullo stato della partita, come il recupero dei punti salute, la modifica temporanea di statistiche oppure la rimozione di determinati effetti negativi. Dopo essere stato utilizzato, uno strumento deve essere consumato e non deve più essere disponibile nei turni successivi.

#### FR-04) Gestione delle abilità del Pokémon
Il sistema deve supportare abilità associate ai Pokémon, che possono modificare il comportamento standard della battaglia. Le abilità devono poter essere attivate in seguito a specifici eventi del gioco, come l'ingresso in campo di un Pokémon, la ricezione di un danno, la fine del turno oppure il verificarsi di condizioni particolari. Gli effetti prodotti da un'abilità possono riguardare diversi aspetti della battaglia, come la modifica del danno inflitto o ricevuto, la cura dei punti salute, l'applicazione o rimozione di stati alterati, la modifica delle condizioni atmosferiche oppure la creazione di immunità rispetto a determinati effetti.

#### FR-05) Gestione dell'ambiente
All'inizio di ogni partita il sistema deve generare una condizione ambientale iniziale casuale, che rimane condivisa tra entrambi i giocatori. L'ambiente deve rappresentare un elemento strategico della battaglia e deve poter modificare il comportamento di alcune mosse o degli effetti applicati durante il combattimento. Il sistema deve prevedere diverse condizioni atmosferiche, tra cui cielo sereno, sole intenso, pioggia, nebbia e tempesta di fulmini. Ogni condizione ambientale può influenzare diversi aspetti della battaglia, come la potenza delle mosse di determinati tipi, la precisione degli attacchi, la possibilità di applicare alcuni stati alterati, il danno residuo prodotto a fine turno oppure la capacità di alcuni Pokémon di recuperare punti salute. Le condizioni ambientali devono poter essere modificate durante la partita, attraverso specifiche mosse o abilità.

### 2.2) Gestione della partita:

#### FR-06) Creazione della squadra
Prima dell'inizio della battaglia, il sistema deve permettere ai giocatori di creare la propria squadra. Ogni squadra deve essere composta esattamente da 6 Pokémon differenti. Non devono essere consentiti duplicati all'interno dello stesso team, mentre lo stesso Pokémon può eventualmente essere scelto da giocatori differenti. Ogni Pokémon selezionato deve possedere 4 mosse configurate prima dell'inizio dello scontro.

#### FR-07) Generazione automatica del team
Il sistema deve permettere la generazione automatica della squadra per consentire agli utenti di iniziare rapidamente una partita senza dover configurare manualmente ogni elemento. Devono essere disponibili due modalità differenti:
- La prima modalità deve effettuare una generazione completamente casuale, selezionando 6 Pokémon dal database disponibile e assegnando casualmente 4 mosse a ciascuno di essi. Questa modalità non deve effettuare valutazioni strategiche relative alla composizione del team, ma deve semplicemente produrre una squadra valida.
- La seconda modalità deve invece utilizzare una logica più evoluta, definita "generazione affine". In questo caso, la selezione delle mosse deve cercare di creare una maggiore coerenza tra il Pokémon scelto e il suo set di mosse. In particolare, devono essere assegnate alcune mosse dello stesso tipo del Pokémon (così da sfruttare il bonus STAB), mentre le altre devono cercare di aumentare la copertura offensiva, permettendo di colpire efficacemente tipologie di avversari contro cui il Pokémon avrebbe normalmente difficoltà.

#### FR-08) Creazione manuale del team
Il sistema deve consentire al giocatore di configurare manualmente la propria squadra. Durante questa fase, l'utente deve poter consultare l'elenco dei Pokémon disponibili e selezionarli uno alla volta, fino al completamento della squadra. Per ogni Pokémon devono essere visualizzabili le informazioni principali, rilevanti per la scelta. Successivamente, il giocatore deve poter selezionare manualmente quattro mosse tra quelle disponibili per ogni Pokémon scelto precedentemente. Anche questa selezione deve avvenire singolarmente, permettendo all'utente di costruire un set di mosse coerente con la strategia desiderata. Il sistema deve impedire configurazioni non valide (ad esempio squadre con meno di sei Pokémon o un Pokémon con un numero di mosse non valido).

#### FR-09) Gestione del turno
Durante ogni turno entrambi i giocatori devono poter scegliere una sola azione relativa al proprio Pokémon attivo. Le azioni disponibili devono essere:
- Utilizzare una delle mosse disponibili del Pokémon in campo.
- Sostituire il Pokémon attivo con uno vivo presente in panchina.
- Utilizzare uno strumento disponibile.

Il sistema deve raccogliere entrambe le scelte prima di procedere alla risoluzione del turno, mantenendo separate le informazioni relative alle decisioni dei due giocatori nella modalità hot-seat. Una volta ricevute entrambe le azioni, il motore di combattimento deve occuparsi automaticamente della loro esecuzione, seguendo le regole definite dal dominio.

#### FR-10) Risoluzione automatica del turno
Il motore di combattimento deve essere responsabile della gestione completa della sequenza di eventi che compongono un turno. La risoluzione deve seguire il seguente ordine:
1) Applicazione degli effetti iniziali del turno.
2) Utilizzo degli strumenti o gestione dei cambi Pokémon.
3) Esecuzione delle mosse.
4) Applicazione degli effetti di fine turno.
Nel caso in cui entrambi i giocatori scelgano di utilizzare una mossa, l'ordine deve essere determinato confrontando la velocità dei Pokémon coinvolti: il Pokémon con velocità maggiore deve agire per primo. Nel caso in cui entrambi i giocatori effettuino azioni non basate sulle mosse (strumenti o sostituzioni), deve essere utilizzato come criterio di risoluzione l'ordine di scelta, eseguendo prima l'azione del giocatore 1 e successivamente quella del giocatore 2. Ogni evento significativo generato durante la risoluzione deve aggiornare correttamente lo stato della battaglia e produrre un messaggio nei log.

#### FR-11) Gestione degli stati alterati
Il sistema deve supportare la gestione degli stati alterati che possono modificare lo stato o il comportamento dei Pokémon durante il combattimento. Gli stati alterati rappresentano condizioni che possono influenzare negativamente il Pokémon coinvolto. Ogni stato alterato deve possedere una propria logica di funzionamento. Alcuni stati possono modificare direttamente le statistiche del Pokémon, mentre altri possono impedire temporaneamente determinate azioni oppure applicare effetti periodici alla fine di ogni turno. Devono essere supportati i seguenti stati: 
- Bruciatura: riduce l'efficacia degli attacchi fisici del Pokémon coinvolto e causa una perdita progressiva di punti salute a fine turno. 
- Veleno: applica un danno residuo che viene calcolato durante la fase di fine turno.
- Paralisi: riduce la capacità di un Pokémon di agire normalmente, introducendo la possibilità che l'azione scelta non venga eseguita.
- Sonno: impedisce temporaneamente l'utilizzo di mosse, fino al verificarsi delle condizioni necessarie per la loro rimozioni (dopo un certo numero di turni o grazie agli strumenti).
- Congelamento: impedisce temporaneamente l'utilizzo di mosse, fino al verificarsi delle condizioni necessarie per la loro rimozioni (dopo un certo numero di turni o grazie agli strumenti).

Gli stati alterati devono essere rappresentati come parte dello stato corrente del Pokémon e devono essere aggiornati automaticamente dal motore di combattimento durante le diverse fasi del turno. Il sistema deve inoltre gestire correttamente la loro eventuale rimozione tramite strumenti, abilità o effetti specifici. Ogni cambiamento relativo agli stati alterati deve essere comunicato attraverso i log della battaglia, indicandone l'applicazione, l'effetto e la rimozione.

#### FR-12) Gestione del calcolo del danno
Il sistema deve implementare un meccanismo di calcolo del danno che tenga conto dei diversi elementi che influenzano il risultato di un attacco. Quando un Pokémon utilizza una mossa offensiva, il sistema deve determinare il danno prodotto, valutando innanzitutto il tipo di mossa utilizzata e la relativa categoria, distinguendo tra attacchi fisici e speciali.
- Nel caso di una mossa fisica devono essere utilizzate la statistica di attacco del Pokémon attaccante e la difesa del Pokémon bersaglio.
- Nel caso di una mossa special devono invece essere considerate la statistica di attacco speciale dell'attaccante e la difesa speciale del difensore.

Il calcolo deve inoltre considerare la potenza base della mossa, eventuali modificatori applicati alle statistiche, il bonus STAB quando il tipo della mossa coincide con uno dei tipi dei Pokémon utilizzatore, l'efficacia della combinazione tra tipo della mossa e tipo del Pokémon avversario, eventuali condizioni ambientali, eventuali abilità dei Pokémon coinvolti e possibili effetti temporanei applicati durante la battaglia.

#### FR-13) Gestione dell'efficacia dei tipi
Il sistema deve implementare il sistema di relazioni tra i diversi tipi di Pokémon, attraverso una matrice di efficacia. Ogni combinazione tra il tipo della mossa utilizzata e il tipo di Pokémon bersaglio deve produrre un determinato modificatore che rappresenta l'efficacia dell'attacco. Devono essere gestiti i seguenti casi:
- Danno super efficace.
- Danno poco efficace.
- Danno con efficacia normale.

Nel caso di un Pokémon con doppio tipo, il sistema deve combinare direttamente gli effetti dei due tipi difensivi, per determinare il moltiplicatore finale applicato al danno.

#### FR-14) Gestione delle modifiche alle statistiche
Il sistema deve permettere alle mosse e agli effetti speciali di modificare temporaneamente le statistiche dei Pokémon durante la battaglia. Ogni Pokémon deve poter mantenere informazioni relative ai modificatori applicati alle proprie caratteristiche principali. Gli effetti possono riguardare statistiche offensive, difensive o relative alla velocità, aumentando o diminuendo temporaneamente il valore utilizzato nei calcoli del combattimento. Durante il calcolo del danno o dell'ordine di esecuzione, il sistema deve utilizzare il valore aggiornato della statistica, considerando tutti i modificatori attualmente attivi.

#### FR-15) Gestione del cambio del Pokémon
Il sistema deve permettere ai giocatori di sostituire il Pokémon attivo con un altro Pokémon (vivo) appartenente alla propria squadra. Il cambio volontario deve essere disponibile come possibile azione durante il turno. Il sistema deve impedire il cambio verso Pokémon già esausti o non disponibili. Quando un Pokémon viene sostituito, devono essere gestiti tutti gli eventi associati al cambio, come l'attivazione di eventuali abilità.

#### FR-16) Gestione del KO
Il sistema deve rilevare automaticamente quando i punti salute di un Pokémon raggiungono lo zero. Quando un Pokémon viene sconfitto, il sistema deve aggiornare correttamente lo stato della squadra e impedire ulteriori azioni da parte del Pokémon eliminato. Dopo un KO, il giocatore interessato deve poter scegliere un nuovo Pokémon tra quelli ancora disponibili nella propria squadra. La battaglia non deve poter continuare fino a quando la sostituzione obbligatoria non è stata completata. 

#### FR-17) Gestione della fine della partita
Il sistema deve controllare continuamente lo stato delle due squadra. Quando un giocatore perde tutti i sei Pokémon disponibili, la partita deve essere considerata conclusa e il sistema deve dichiarare automaticamente il vincitore.

#### FR-18) Gestione dei log della partita
Il sistema deve mantenere un log cronologico contenente tutti gli eventi significativi avvenuti durante lo svolgimento della partita. Il log deve permettere al giocatore di comprendere l'evoluzione della battaglia e deve includere informazioni relative all'utilizzo delle mosse, ai danni inflitti, al fallimento degli attacchi, all'applicazione o rimozione degli stati alterati, ai cambi, all'utilizzo di strumenti, alle modifiche del meteo, all'attivazione di abilità e alla conclusione della partita.

#### FR-19) Gestione dell'interfaccia grafica
Il sistema deve fornire un'interfaccia grafica minimale, permetta ai giocatori di configurare la partita e interagire con il combattimento. 
- Durante la preparazione della partita deve consentire la selezione della modalità della creazione della squadra, la scelta dei Pokémon, la configurazione delle mosse e la visualizzazione delle informazioni necessarie alla costruzione del team. 
- Durante la battaglia devono essere mostrate tutte le informazioni fondamentali per comprendere lo stato corrente dello scontro (principalmente riprese dai log).

L'interfaccia deve inoltre supportare correttamente la modalità hot-seat, indicando chiaramente il momento in cui il controllo passa da un giocatore all'altro.

## 3) Requisiti non funzionali:

### NFR-01) Usabilità
L'interfaccia del sistema deve permettere agli utenti di comprendere facilmente lo stato della partita e di effettuare scelte senza ambiguità. Durante il combattimento il giocatore deve avere sempre accesso alle informazioni necessarie per prendere decisioni strategiche, come i punti salute del Pokémon corrente, le mosse disponibili, i PP rimanenti, gli strumenti utilizzabili e le condizioni ambientali attive. La modalità hot-seat richiede che i due utenti utilizzino lo stesso dispositivo, ma mantenendo separate le proprie informazioni strategiche.

### NFR-02) Affidabilità
Il sistema deve garantire la consistenza dello stato della partita durante tutta la durata del combattimento. Ogni azione effettuata dai giocatori deve produrre una transizione valida dello stato del gioco, evitando situazioni impossibili.

### NFR-03) Estendibilità
Il sistema deve rappresentare un esempio di applicazione pratica dei paradigmi di programmazione funzionale studiati durante il corso. L'architettura deve essere progettata in modo da permettere l'introduzione futura di nuovi elementi del gioco (ad esempio Pokémon aggiuntivi, nuove mosse, nuovi strumenti o nuove condizioni ambientali), senza richiedere modifiche invasive alle componenti già esistenti.

### NFR-04) Tracciabilità
Ogni modifica significativa dello stato della partita deve essere accompagnata da una corrispondente informazione nel log della battaglia. Gli utenti devono poter comprendere il motivo per cui una determinata situazione si è verificata.

## 4) Requisiti di implementazione:

### IR-01) Linguaggio di sviluppo
Il sistema deve essere sviluppato utilizzando il linguaggio Scala 3 come linguaggio principale. In particolare, il progetto deve fare uso delle funzionalità introdotte nelle versioni più recenti del linguaggio, come ad esempio i meccanismi di gestione del contesto, i tipi algebrici e le estensioni dei tipi. La versione specifica del linguaggio Scala deve essere la 3.4.2

### IR-02) Sistema di build
La gestione della compilazione, delle dipendenze e dell'esecuzione del progetto deve essere effettuata attraverso *SBT* (*Scala Build Tool*). La configurazione del progetto deve permettere di compilare il codice sorgente, eseguire l'applicazione principale, eseguire la suite di test automatizzati e gestire le librerie esterne necessarie.

### IR-03) Organizzazione architetturale
L'implementazione deve seguire una struttura modulare nella quale ogni componente del sistema abbia una responsabilità ben definita. Il progetto deve presentare una parte di dominio (rappresentazione delle entità principali del gioco e delle relative regole statiche), una parte di logica applicativa (che si occupa della gestione dello stato della partita e delle operazioni che modificano il comportamento del sistema), un livello applicativo (che si occupa di coordinare il funzionamento generale del programma e collegare la logica di gioco con l'interfaccia) e una parte di interfaccia grafica (responsabile solamente della presentazione delle informazioni all'utente e della raccolta delle azioni effettuate tramite la stessa).

### IR-04) Utilizzo del paradigma funzionale
L'implementazione deve seguire un approccio prevalentemente funzionale, limitando il più possibile la presenza di stato modificabile. In particolare, una scelta effettuata durante un turno deve produrre una nuova configurazione della battaglia anziché modificare direttamente quella precedente.

### IR-05) Gestione dei contenuti di gioco tramite DSL
Le informazioni relative agli elementi del dominio devono essere definite attraverso strutture dichiarative. Il progetto deve utilizzare *DSL* (*Domain Specific Language*) interno per permettere di descrivere i contenuti del gioco in modo leggibile.