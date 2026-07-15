# Specifica dei requisiti

## 1) Requisiti di business:

### BR-01) Simulazione di combattimenti tattici:
Il sistema deve permettere a due giocatori di affrontarsi in combattimenti Pokémon uno contro uno, caratterizzati da una componente strategica significativa. La battaglia deve essere basata sulla scelta delle azioni effettuate durante ogni turno e non sulla semplice esecuzione automatica degli attacchi. Il giocatore deve poter prendere decisioni tenendo conto di diversi fattori, tra cui la composizione della squadra, la scelta delle mosse disponibili, il consumo delle risorse, i punti di forza e debolezza dei diversi Pokémon e delle diverse mosse, le condizioni ambientali presenti durante lo scontro e gli eventuali stati alterati applicati ai Pokémon. Il sistema deve inoltre garantire un'esperienza dove la vittoria dipenda dalla capacità del giocatore di pianificare le proprie mosse e adattarsi alle situazioni generate durante la partita.

### BR-02) Personalizzazione della squadra:
Il sistema deve consentire ai giocatori di costruire la propria squadra prima dell'inizio della battaglia. Ogni giocatore deve poter disporre di una squadra composta da 6 Pokémon differenti, evitando quindi duplicati all'interno della stessa squadra. La creazione deve poter avvenire attraverso modalità differenti, permettendo sia un inizio rapido, sia una scelta più approfondita, adattandosi così agli utenti interessati solamente alla simulazione e a coloro che vogliono strategie specifiche.
- La prima modalità deve consentire una generazione automatica della squadra, utile per iniziare rapidamente una partita, senza dover configurare manualmente ogni elemento.
- La seconda modalità deve invece permettere al giocatore una personalizzazione completa, dando la possibilità di scegliere direttamente i Pokémon e le relative mosse.

### BR-03) Esperienza di gioco in modalità hot-seat:
Il sistema deve supportare una modalità multiplayer locale, nella quale due utenti utilizzano lo stesso dispositivo, senza necessità di una connessione di rete. Durante la fase di selezione delle azioni, il sistema deve garantire che ogni giocatore possa effettuare le proprie decisioni, venendo avvisato del cambio del turno di scelta, così da garantire la segretezza di tale azione. La modalità hot-seat prevede che entrambi gli utenti condividano lo stesso schermo, ma che mantengano comunque segrete le proprie scelte. Il sistema deve quindi gestire correttamente il passaggio del controllo tra i due giocatori, mostrando un'apposita indicazione quando è necessario cambiare utente.

### BR-04) Riproduzione coerente delle regole di combattimento:
Il sistema deve garantire che la simulazione della battaglia produca risultati coerenti con le regole definite dal dominio implementato. Ogni turno deve essere risolto seguendo un insieme preciso di regole che determinano l'ordine delle azioni e degli effetti prodotti. La risoluzione deve considerare elementi quali la priorità delle mosse, la velocità dei Pokémon coinvolti, gli effetti dovuti alle condizioni ambientali, gli stati alterati attivi e la gestione delle situazioni di KO. A parità di condizioni iniziali e di risultati casuali generati, il motore di combattimento deve produrre sempre lo stesso risultato.

## 2) Requisiti funzionali:

### 2.1) Gestione del dominio

#### FR-01) Gestione Pokémon
Il sistema deve rappresentare ogni Pokémon attraverso un modello che distingua chiaramente le informazioni statiche (che definiscono la specie del Pokémon) da quelle dinamiche (che variano durante la battaglia).
- Le informazioni statiche devono comprendere il nome del Pokémon, il tipo di appartenenza, le statistiche di base che ne determinano le caratteristiche in combattimento (punti salute / HP, attacco, difesa, attacco speciale, difesa speciale, velocità) e uno slot di abilità passive (abilità primaria e una tra abilità secondaria o nascosta). Rappresentano le caratteristiche intrinseche (visibili come valori massimi a cui fare riferimento) e non devono essere modificate durante il combattimento.
- Le informazioni dinamiche devono comprendere i punti salute correnti, gli eventuali stati alterati attivi, i modificatori applicati alle statistiche e le mosse disponibili.

#### FR-02) Gestione delle mosse
Ogni Pokémon deve possedere un insieme di quattro mosse, utilizzabili durante il combattimento. Le mosse devono essere rappresentate attraverso un modello che distingua le informazioni statiche (che definiscono le caratteristiche intrinseche della mossa) dalle informazioni dinamiche (che possono variare durante lo svolgimento della battaglia). Le mosse devono prevedere inoltre una base comune che permetta di gestire sia attacchi offensivi, sia mosse con effetto di supporto o modifica dello stato della battaglia (*mosse status*), che non causano danno diretto.
- Le informazioni statiche devono comprendere il nome della mossa, il tipo di appartenenza, la categoria (*fisico*, *speciale* oppure *status*), il numero massimo di utilizzi disponibili (*PP*), il valore della precisione (valore probabilistico percentuale di riuscita della mossa), l'eventuale potenza base (presente solo nelle mosse offensive) e l'eventuale effetto associato alla mossa. Per le mosse che infliggono danno l'effetto rappresenta un possibile effetto collaterale opzionale, mentre per le mosse di tipo status costituisce l'effetto principale della mossa (quindi obbligatorio). Tali informazioni rappresentano la definizione della mossa e non devono essere modificate durante il combattimento. 
- Le informazioni dinamiche devono rappresentare lo stato corrente della mossa in utilizzo durante la battaglia. In particolare, il sistema deve mantenere il numero di PP rimanenti, aggiornandolo continuamente dopo ogni utilizzo, e un riferimento alla corrispondente definizione statica della mossa. Inoltre il sistema deve consentire la modifica temporanea di alcune caratteristiche della mossa durante il combattimento, come ad esempio la precisione, quando previsto da eventuali abilità, mosse o altre meccaniche di gioco, mantenendo comunque inalterata la definizione originale.

Il sistema deve considerare la possibilità che una mossa fallisca sulla base del proprio valore di precisione. Inoltre, il sistema deve impedire l'utilizzo di una mossa quando questa non dispone più di PP disponibili, comunicando opportunamente al giocatore il motivo per cui l'azione non può essere eseguita.

#### FR-03) Gestione degli strumenti consumabili
Il sistema deve fornire a ogni giocatore un insieme iniziale di strumenti utilizzabili durante la battaglia. Gli strumenti devono essere generati casualmente all'inizio della partita e devono rappresentare una risorsa limitata che il giocatore può utilizzare strategicamente durante lo scontro. Durante il proprio turno, il giocatore deve poter scegliere di utilizzare uno strumento disponibile invece di eseguire una mossa o invece di scambiare il proprio Pokémon. Ogni strumento deve avere un effetto specifico sullo stato della partita, come il recupero dei punti salute, la modifica temporanea di statistiche oppure la rimozione di determinati effetti negativi. Dopo essere stato utilizzato, uno strumento deve consumarsi e non deve quindi più essere disponibile nei turni successivi.

#### FR-04) Gestione delle abilità del Pokémon
Il sistema deve supportare abilità passive associate ai Pokémon, che possono modificare il comportamento standard della battaglia. Le abilità devono poter essere attivate automaticamente in seguito a specifici eventi del gioco. In particolare, il sistema deve supportare le seguenti modalità di attivazione: 
- All'inizio o alla fine del turno.
- All'ingresso in campo di un Pokémon (proprio o avversario).
- All'uscita dal campo di un Pokémon (proprio o avversario).
- Quando un Pokémon subisce un danno (proprio o avversario).
- Quando un Pokémon viene mandato KO (proprio o avversario).

Gli effetti prodotti da un'abilità possono riguardare diversi aspetti della battaglia, come la modifica del danno inflitto o ricevuto, il recupero dei punti salute, l'applicazione o la rimozione di stati alterati, la modifica delle condizioni atmosferiche oppure la concessione di immunità rispetto a determinati effetti. Il sistema deve garantire che l'attivazione delle abilità avvenga automaticamente nel momento corretto della battaglia, aggiornando coerentemente lo stato del combattimento e registrando gli effetti prodotti nel log della partita.

#### FR-05) Gestione dell'ambiente
All'inizio di ogni partita il sistema deve generare una condizione ambientale iniziale casuale, che rimane condivisa tra entrambi i giocatori. L'ambiente deve rappresentare un elemento strategico della battaglia e deve poter modificare il comportamento di alcune mosse o degli effetti applicati durante il combattimento. Il sistema deve prevedere le seguenti condizioni atmosferiche con i relativi effetti: 
- Cielo sereno: rappresenta la condizione atmosferica neutrale. Non applica modificatori, non altera la precisione e non produce alcun effetto.
- Sole intenso: le mosse di tipo fuoco infliggono il 70% di danno in più, le mosse di tipo acqua infliggono il 30% di danno in meno, lo stato Congelamento non può essere applicato e il danno residuo del veleno viene aumentato del 50%.
- Pioggia: le mosse di tipo acqua infliggono il 30% di danno in più, le mosse di tipo fuoco infliggono il 30% di danno in meno, le mosse di tipo elettro ignorano il controllo di precisione e i Pokémon di tipo fuoco subiscono un danno residuo pari a 1/16 degli HP massimi alla fine di ogni turno.
- Nebbia: la precisione di tutte le mosse viene ridotta all'80% del valore normale, le mosse di tipo psico che possono causare sonno hanno una probabilità aumentata del 10% e i Pokémon di tipo psico recuperano 1/16 degli HP massimi alla fine di ogni turno.
- Tempesta di fulmini: le mosse di tipo elettro infliggono il 30% di danno in più, le mosse di tipo erba infliggono il 30% di danno in meno, le mosse di tipo elettro che possono causare paralisi hanno una probabilità fissa del 70% e tutti i Pokémon che non sono di tipo elettro subiscono un danno residuo pari a 1/16 degli HP massimi alla fine di ogni turno.

Ogni condizione ambientale può influenzare diversi aspetti della battaglia, come la potenza delle mosse di determinati tipi, la precisione degli attacchi, la possibilità di applicare alcuni stati alterati, il danno residuo prodotto a fine turno oppure la capacità di alcuni Pokémon di recuperare punti salute. Le condizioni ambientali devono poter essere modificate durante la partita, attraverso specifiche abilità.

### 2.2) Gestione della partita:

#### FR-06) Gestione della difficoltà della partita
Il sistema deve permettere al giocatore di selezionare il livello di difficoltà della partita, prima dell'inizio del combattimento. La difficoltà deve rappresentare un parametro generale che permette di modificare il comportamento del sistema di combattimento, influenzando il calcolo del danno prodotto durante gli attacchi, applicando un opportuno modificatore che consente di ottenere combattimenti con livelli di sfida differenti. Il sistema deve supportare i seguenti livelli di difficoltà:
- Facile: il danno inflitto dalle mosse deve essere calcolato applicando un moltiplicatore pari a 0.1.
- Medio: il danno inflitto dalle mosse deve essere calcolato applicando un moltiplicatore pari a 0.2.
- Difficile: il danno inflitto dalle mosse deve essere calcolato applicando un moltiplicatore pari a 0.3.

La scelta della difficoltà deve essere effettuata durante la fase di configurazione iniziale della partita e deve rimanere invariata per tutta la durata dello scontro. Il sistema deve garantire che il valore selezionato venga utilizzato automaticamente durante tutte le operazioni di combattimento interessate.

#### FR-07) Creazione della squadra
Prima dell'inizio della battaglia, il sistema deve permettere ai giocatori di creare la propria squadra. Ogni squadra deve essere composta esattamente da 6 Pokémon differenti. Non devono essere consentiti duplicati all'interno dello stesso team, mentre lo stesso Pokémon può eventualmente essere scelto da giocatori differenti. Ogni Pokémon selezionato deve possedere 4 mosse configurate prima dell'inizio dello scontro.

#### FR-07a) Generazione automatica del team
Il sistema deve permettere la generazione automatica della squadra per consentire agli utenti di iniziare rapidamente una partita senza dover configurare manualmente ogni elemento. Devono essere disponibili due modalità differenti:
- La prima modalità deve effettuare una generazione completamente casuale, selezionando 6 Pokémon dal database disponibile e assegnando casualmente 4 mosse a ciascuno di essi. Questa modalità non deve effettuare valutazioni strategiche relative alla composizione del team, ma deve semplicemente produrre una squadra valida.
- La seconda modalità deve invece utilizzare una logica più evoluta, definita "generazione affine". In questo caso, la selezione delle mosse deve cercare di creare una maggiore coerenza tra il Pokémon scelto e il suo set di mosse. In particolare, devono essere assegnate alcune mosse dello stesso tipo del Pokémon (così da sfruttare il bonus STAB), mentre le altre devono cercare di aumentare la copertura offensiva, permettendo di colpire efficacemente tipologie di avversari contro cui il Pokémon avrebbe normalmente difficoltà.

#### FR-07b) Creazione manuale del team
Il sistema deve consentire al giocatore di configurare manualmente la propria squadra. Durante questa fase, l'utente deve poter consultare l'elenco dei Pokémon disponibili e selezionarli uno alla volta, fino al completamento della squadra. Per ogni Pokémon devono essere visualizzabili le informazioni principali, rilevanti per la scelta. Successivamente, il giocatore deve poter selezionare manualmente quattro mosse tra quelle disponibili, per ogni Pokémon scelto precedentemente. Anche questa selezione deve avvenire singolarmente, permettendo all'utente di costruire un set di mosse coerente con la strategia desiderata. Il sistema deve impedire configurazioni non valide (ad esempio squadre con meno di sei Pokémon o un Pokémon con un numero di mosse non valido).

#### FR-08) Gestione dei log della partita
Il sistema deve mantenere un log cronologico contenente tutti gli eventi significativi avvenuti durante lo svolgimento della partita. Il log deve permettere al giocatore di comprendere l'evoluzione della battaglia e deve includere informazioni relative all'utilizzo delle mosse, ai danni inflitti, al fallimento degli attacchi, all'applicazione o rimozione degli stati alterati, ai cambi, all'utilizzo di strumenti, alle modifiche del meteo, all'attivazione di abilità e alla conclusione della partita.

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
Il sistema deve supportare la gestione degli stati alterati che possono modificare lo stato o il comportamento dei Pokémon durante il combattimento. Gli stati alterati rappresentano condizioni che influenzano temporaneamente o permanentemente il Pokémon coinvolto. Il sistema deve prevedere stati alterati che rimangono attivi fino alla loro rimozione e stati temporanei che possiedono una durata limitata (espressa in numero di turni) e vengono rimossi automaticamente al termine della loro durata. Devono essere supportati i seguenti stati: 
- Bruciatura: causa una perdita progressiva di punti salute al termine di ogni turno, pari a 1/8 degli HP massimi del Pokémon affetto. 
- Veleno: applica un danno residuo pari a 1/8 degli HP massimi del Pokémon affetto durante la fase di fine turno.
- Paralisi: introduce una probabilità (25%) che il Pokémon non riesca a eseguire l'azione selezionata durante il turno.
- Congelamento: impedisce al Pokémon di utilizzare le proprie mosse fino allo scongelamento, che avviene in maniera probabilistica (10% di probabilità) o quando particolari condizioni della battaglia lo consentono.
- Sonno: impedisce temporaneamente al Pokémon di utilizzare le proprie mosse. Lo stato permane per un numero casuale di turni e viene rimosso automaticamente al termine della sua durata (da 1 a 4 turni) oppure mediante l'utilizzo di specifici strumenti.
- Confusione: introduce una probabilità (50%) che il Pokémon colpisca sè stesso con mosse dannose, invece dell'avversario. Lo stato permane per un numero limitato di turni (da 2 a 5) e viene rimosso automaticamente allo scadere della sua durata.
- Ricarica: rappresenta uno stato temporaneo utilizzato dalle mosse che richiedono dei turni di ripresa dopo la propria esecuzione. Durante questo stato il Pokémon non può effettuare azioni e la condizione viene rimossa automaticamente al termine delle fasi necessarie.

Gli stati alterati devono essere rappresentati come parte dello stato dinamico corrente del Pokémon e devono essere aggiornati automaticamente dal motore di combattimento durante le diverse fasi del turno. Il sistema deve inoltre gestire correttamente la loro eventuale rimozione tramite strumenti, abilità o effetti specifici. Ogni cambiamento relativo agli stati alterati deve essere comunicato attraverso i log della battaglia, indicandone l'applicazione, l'effetto e la rimozione.

#### FR-12) Gestione del calcolo del danno
Il sistema deve implementare un meccanismo di calcolo del danno prodotto da una mossa offensiva, che tenga conto dei diversi fattori che possono influenzarne l'esito. Quando un Pokémon utilizza una mossa offensiva, il sistema deve determinare il danno prodotto attraverso la seguente formula:

$$
D = 
\left(
Base \cdot STAB \cdot TypeEff \cdot PolicyMult \cdot WeatherMult \cdot CriticalMult \cdot AttModifier \cdot DefModifier
\right)
$$

Dove il valore di base del danno viene ottenuto tramite:

$$
Base =
\left(
\frac{
\left(\frac{2 \cdot L}{5}+2\right)
\cdot P
\cdot
\frac{A}{D}
}{
50
}
+2
\right)
$$

Dove:
- $L$ rapresenta il livello del Pokémon attaccante (nel sistema impostato a 50).
- $P$ rappresenta la potenza della mossa utilizzata.
- $A$ rappresenta la statistica offensiva del Pokémon attaccante.
- $D$ rappresenta la statistica difensiva del Pokémon bersaglio.
- $STAB$ rappresenta il bonus "Same Type Attack Bonus" (pari a 1.5 quando il tipo della mossa coincide con il tipo del Pokémon attaccante, altrimenti pari a 1).
- $TypeEff$ rappresenta il moltiplicatore dell'efficacia del tipo della mossa contro il tipo del Pokémon avversario.
- $PolicyMult$ rappresenta il moltiplicatore globale definito dalla difficoltà selezionata.
- $WeatherMult$ rappresenta il moltiplicatore derivante dalle condizioni atmosferiche presenti nella battaglia.
- $CriticalMult$ rappresenta il moltiplicatore relativo ai colpi critici, pari a 1.5 in caso di colpo critico e 1 altrimenti.
- $AttModifier$ rappresenta il modificatore applicato dalle abilità del Pokémon attaccante.
- $DefModifier$ rappresenta il modificatore applicato dalle abilità del Pokémon difensore.

La statistica offensiva e quella difensiva utilizzate nella formula dipendono dalla categoria della mossa:
- Nel caso di una mossa fisica devono essere utilizzate la statistica di attacco del Pokémon attaccante e la difesa del Pokémon bersaglio.
- Nel caso di una mossa speciale devono invece essere considerate la statistica di attacco speciale dell'attaccante e la difesa speciale del difensore.

Nel caso di un colpo critico, il sistema modifica inoltre le statistiche considerate nel calcolo:
- La statistica offensiva dell'attaccante non può essere inferiore al valore base della specie.
- La statistica difensiva del difensore non può essere superiore al valore base della specie.

Il sistema deve determinare casualmente se un attacco risulta critico, utilizzando la probabilità base del 6.25%, eventualmente modificata da effetti specifici della mossa o da altri effetti.

#### FR-13) Gestione dell'efficacia dei tipi
Il sistema deve implementare un meccanismo per determinare l'efficacia di una mossa offensiva in base alla relazione tra il tipo della mossa utilizzata e il tipo del Pokémon bersaglio. Le relazioni tra i tipi sono definite attraverso una tabella di efficacia che associa ogni coppia composta dal tipo della mossa e dal tipo del Pokémon difensore a un valore di efficacia. L'efficacia di una combinazione tra tipo della mossa e tipo del bersaglio può assumere uno dei seguenti valori:

| Efficacia | Moltiplicatore del danno | Descrizione|
|-----------|--------------------------|------------|
| Nessun effetto | 0.0 | Il danno viene annullato. |
| Poco efficace | 0.5 | Il danno prodotto viene dimezzato. |
| Normale | 1.0 | Il danno non viene modificato. |
| Super efficace | 2.0 | Il danno prodotto viene raddoppiato. |

Durante il calcolo del danno, il moltiplicatore ottenuto viene quindi applicato alla formula del danno finale, insieme agli altri modificatori previsti. 

La tabella delle relazioni non neutrali tra tipi è la seguente:

| Attacco/difesa | Fuoco | Acqua | Erba | Elettro | Psico | Veleno |
|----------------|-------|-------|------|---------|-------|--------|
| Fuoco          | 0.5   | 0.5   | 2.0  | 1.0     | 1.0   | 1.0    |
| Acqua          | 2.0   | 0.5   | 0.5  | 1.0     | 1.0   | 1.0    |
| Erba           | 0.5   | 2.0   | 0.5  | 1.0     | 1.0   | 0.5    |
| Elettro        | 1.0   | 2.0   | 0.5  | 0.5     | 1.0   | 1.0    |
| Psico          | 1.0   | 1.0   | 1.0  | 1.0     | 0.5   | 1.0    |
| Veleno         | 1.0   | 1.0   | 2.0  | 1.0     | 1.0   | 1.0    |

Per tutte le combinazioni non esplicitamente definite nella tabella delle relazioni, il sistema deve considerare l'efficacia come normale.

#### FR-14) Gestione delle modifiche alle statistiche
Il sistema deve permettere alle mosse e agli effetti speciali di modificare temporaneamente le statistiche dei Pokémon durante la battaglia (informazioni dinamiche del Pokèmon). Ogni Pokémon deve poter mantenere informazioni relative ai modificatori applicati alle proprie caratteristiche principali. Gli effetti possono riguardare statistiche offensive, difensive o relative alla velocità, aumentando o diminuendo temporaneamente il valore utilizzato nei calcoli del combattimento. Durante il calcolo del danno o dell'ordine di esecuzione, il sistema deve utilizzare il valore aggiornato della statistica, considerando tutti i modificatori attualmente attivi.

#### FR-15) Gestione del cambio del Pokémon
Il sistema deve permettere ai giocatori di sostituire il Pokémon attivo con un altro Pokémon (vivo) appartenente alla propria squadra. Il cambio volontario deve essere disponibile come possibile azione durante il turno. Il sistema deve impedire lo scambio verso Pokémon esausti o non disponibili. Quando un Pokémon viene sostituito, devono essere gestiti tutti gli eventi associati al cambio, come l'attivazione di eventuali abilità.

#### FR-16) Gestione del KO
Il sistema deve rilevare automaticamente quando i punti salute di un Pokémon raggiungono lo zero. Quando un Pokémon viene sconfitto, il sistema deve aggiornare correttamente lo stato della squadra e impedire ulteriori azioni da parte del Pokémon eliminato. Dopo un KO, il giocatore interessato deve poter scegliere un nuovo Pokémon tra quelli ancora disponibili nella propria squadra. La battaglia non deve poter continuare fino a quando la sostituzione obbligatoria non è stata completata. 

#### FR-17) Gestione della fine della partita
Il sistema deve controllare continuamente lo stato delle due squadra. Quando un giocatore perde tutti i Pokémon a propria disposizione, la partita deve essere considerata conclusa e il sistema deve dichiarare automaticamente il vincitore.

#### FR-18) Gestione dell'interfaccia grafica
Il sistema deve fornire un'interfaccia grafica minimale, che permetta ai giocatori di configurare la partita e interagire con il combattimento. 
- Durante la preparazione della partita deve consentire la selezione della difficoltà della partita e della modalità della creazione della squadra, la scelta dei Pokémon, la configurazione delle mosse e la visualizzazione delle informazioni necessarie alla costruzione del team. 
- Durante la battaglia devono essere mostrate tutte le informazioni fondamentali per comprendere lo stato corrente dello scontro (principalmente riprese dai log) e devono essere presenti meccanismi di interazione disponibili all'utente per selezionare l'azione del turno corrente.

L'interfaccia deve inoltre supportare correttamente la modalità hot-seat, indicando chiaramente il momento in cui il controllo passa da un giocatore all'altro.

## 3) Requisiti non funzionali:

### NFR-01) Usabilità
L'interfaccia del sistema deve permettere agli utenti di comprendere facilmente lo stato della partita e di effettuare scelte senza ambiguità. Durante il combattimento il giocatore deve avere sempre accesso alle informazioni necessarie per prendere decisioni strategiche, come i punti salute del Pokémon corrente, le mosse disponibili, i PP rimanenti, gli strumenti utilizzabili e le condizioni ambientali attive. La modalità hot-seat richiede che i due utenti utilizzino lo stesso dispositivo, ma mantenendo separate le proprie informazioni strategiche.

### NFR-02) Affidabilità
Il sistema deve garantire la consistenza dello stato della partita durante tutta la durata del combattimento. Ogni azione effettuata dai giocatori deve produrre una transizione valida dello stato del gioco, evitando situazioni impossibili.

### NFR-03) Estendibilità
Il sistema deve rappresentare un esempio di applicazione pratica costruita mediante i paradigmi di programmazione funzionale studiati durante il corso. L'architettura deve essere progettata in modo da permettere l'introduzione futura di nuovi elementi del gioco (ad esempio Pokémon aggiuntivi, nuove mosse, nuovi strumenti o nuove condizioni ambientali), senza richiedere modifiche invasive alle componenti già esistenti.

### NFR-04) Tracciabilità
Ogni modifica significativa dello stato della partita deve essere accompagnata da una corrispondente informazione nel log della battaglia. Gli utenti devono poter comprendere il motivo per cui una determinata situazione si è verificata.

## 4) Requisiti di implementazione:

### IR-01) Linguaggio di sviluppo
Il sistema deve essere sviluppato utilizzando il linguaggio Scala 3 come linguaggio principale. In particolare, il progetto deve fare uso delle funzionalità introdotte nelle versioni più recenti del linguaggio, come ad esempio i meccanismi di gestione del contesto, i tipi algebrici e le estensioni dei tipi. La versione specifica del linguaggio Scala deve essere la 3.4.2.

### IR-02) Sistema di build
La gestione della compilazione, delle dipendenze e dell'esecuzione del progetto deve essere effettuata attraverso *SBT* (*Scala Build Tool*). La configurazione del progetto deve permettere di compilare il codice sorgente, eseguire l'applicazione principale, eseguire la suite di test automatizzati e gestire le librerie esterne necessarie.

### IR-03) Organizzazione architetturale
L'implementazione deve seguire una struttura modulare nella quale ogni componente del sistema abbia una responsabilità ben definita. Il progetto deve presentare una parte di dominio (rappresentazione delle entità principali del gioco e delle relative regole statiche), una parte di logica applicativa (che si occupa della gestione dello stato della partita e delle operazioni che modificano il comportamento del sistema), un livello applicativo (che si occupa di coordinare il funzionamento generale del programma e collegare la logica di gioco con l'interfaccia) e una parte di interfaccia grafica (responsabile solamente della presentazione delle informazioni all'utente e della raccolta delle azioni effettuate tramite la stessa).

### IR-04) Utilizzo del paradigma funzionale
L'implementazione deve seguire un approccio prevalentemente funzionale, limitando il più possibile la presenza di stato modificabile. In particolare, una scelta effettuata durante un turno deve produrre una nuova configurazione della battaglia anziché modificare direttamente quella precedente.

### IR-05) Gestione dei contenuti di gioco tramite DSL
Le informazioni relative agli elementi del dominio devono essere definite attraverso strutture dichiarative. Il progetto deve utilizzare *DSL* (*Domain Specific Language*) interno per permettere di descrivere i contenuti del gioco in modo leggibile.