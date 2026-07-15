# Abstract

Scalamon è un sistema software progettato per simulare combattimenti tattici a turni in modalità hot-seat, 
ispirato alle meccaniche dei battle game a squadre. In particolare è stato preso come modello quello del videogioco
online Pokémon Showdown. L’obiettivo del progetto è la realizzazione di un motore di battaglia completo, capace di
gestire in modo coerente e verificabile tutte le fasi principali di uno scontro tra due giocatori: dalla preparazione
della squadra fino alla determinazione dell’esito finale della partita. Il sistema è stato concepito per offrire
un’esperienza di gioco strutturata, in cui le regole del dominio vengano modellate in maniera esplicita e dove la
logica di esecuzione risulti sufficientemente chiara, estendibile e manutenibile.

Ogni partita coinvolge due giocatori che, prima dell’inizio della battaglia, devono costruire una squadra composta da
sei Pokémon. Questa può essere scelta manualmente dell'utente oppure casualmente dal sistema.

Ogni Pokémon è caratterizzato da: statistiche specifiche (in particolare: salute, attacco, difesa, attacco speciale,
difesa speciale e velocità), un tipo (tra: Normale, Fuoco, Acqua, Erba, Elettro, Psico, Veleno) e un'abilità passiva.
Questa fase di preparazione è fondamentale perché incide direttamente sulle strategie possibili e sul bilanciamento
dell’incontro. Ogni turno del combattimento richiede ai giocatori di selezionare un’azione tra quelle disponibili.
In particolare vi sono tre possibilità. La prima è quella di utilizzare la mossa di un Pokémon, anch'essa scelta dal
giocatore o dal software durante la fase di costruzione della squadra. Ogni mossa è caratterizzata da: Power Points
(che definiscono il numero massimo di volte che una mossa può essere utilizzata), accuratezza e tipo. Ogni utilizzo di
una mossa ha il costo di 1 PP. La seconda possibilità è quella di cambiare Pokémon attivo in combattimento, scegliendo
fra quelli non esausti. Infine è possibile usare uno strumento per influenzare il proprio Pokémon attivo. Ogni strumento
ha un solo utilizzo e ogni giocatore ne possiede 8 scelti casualmente. Il sistema, quindi, raccoglie le scelte
effettuate, stabilisce l’ordine di esecuzione sulla base di priorità e velocità di ogni azione e aggiorna lo stato
globale della battaglia in modo consistente.

Uno degli aspetti più significativi del progetto riguarda la gestione dell’ordine dei turni e dell’esecuzione delle
azioni. La precedenza di una particolare azione rispetto ad un'altra non dipende esclusivamente dalla velocità del
Pokémon che la esegue, ma può essere influenzata anche da ulteriori elementi del dominio, come la priorità intrinseca
delle mosse, le abilità speciali, le condizioni atmosferiche e gli stati alterati presenti sul campo. Questa scelta
progettuale consente al sistema di riprodurre un insieme di interazioni ricco e realistico, in cui il risultato di un
turno non è determinato da una semplice sequenza lineare, ma da una combinazione di regole che devono essere valutate
e coordinate con precisione. Dopo la determinazione dell’ordine, il motore procede con: calcolo dei danni, applicazione
degli effetti secondari eventualmente applicati alle mosse, aggiornamento degli stati alterati, gestione di eventuali
KO, con annessa sostituzione forzata del Pokémon esausto.

Il progetto include un insieme di componenti che costituiscono il dominio applicativo. Tra questi rientrano i Pokémon
come entità principali, le mosse, gli stati alterati come effetti in grado di modificare l’andamento della battaglia,
il meteo che può influenzare le statistiche dei Pokémon e gli strumenti che danno la possibilità di modificare le
condizioni del Pokémon attivo di un giocatore. L’obiettivo è modellare questi elementi non soltanto come dati statici,
ma come parti di un sistema dinamico capace di evolversi durante l’esecuzione del gioco. In questo senso, il progetto
mira a garantire che ogni turno produca un aggiornamento coerente dello stato della partita, rendendo il comportamento
del sistema prevedibile dal punto di vista formale ma comunque ricco dal punto di vista strategico.

Dal punto di vista dell’esperienza d’uso, Scalamon prevede un’interfaccia grafica minimale, con l’obiettivo di rendere
il flusso di gioco chiaro e immediato. L’interazione è centrata sulla comprensione delle scelte possibili, sulla
visualizzazione delle informazioni rilevanti e sulla restituzione di un log testuale degli eventi di battaglia, utile
sia al giocatore sia alla verifica del corretto funzionamento del sistema. La presenza di un log dettagliato è
particolarmente utile in un progetto di questo tipo, perché consente di osservare in modo trasparente l’effetto di
ogni decisione e di controllare se le regole implementate vengono applicate correttamente. Un sistema di questo genere
deve infatti saper comunicare all’utente non solo l’esito delle azioni, ma anche la motivazione di tale esito,
soprattutto nei casi in cui intervengano priorità, abilità o stati alterati che modificano la sequenza attesa degli
eventi.

La bontà del progetto può essere valutata attraverso una serie di caratteristiche funzionali e qualitative. In primo
luogo, il sistema deve essere corretto nel calcolo delle regole di combattimento: danni, ordine delle azioni, effetti
secondari, gestione degli stati e condizioni di vittoria devono comportarsi in modo coerente con le specifiche del
dominio. In secondo luogo, il progetto deve risultare capace di gestire correttamente situazioni limite come,
l’esaurimento delle mosse disponibili o la necessità di sostituzioni forzate. Un ulteriore indicatore di qualità è la
chiarezza dell’architettura software: il sistema dovrebbe essere organizzato in componenti ben separati, in modo da
facilitare: la comprensione del codice, l’estensione futura delle funzionalità e l’individuazione di eventuali difetti.
Infine, un buon risultato si potrebbe misurare anche nella qualità dell’esperienza complessiva, che deve essere fluida,
leggibile e sufficientemente intuitiva da permettere ai giocatori di concentrarsi sulla strategia senza essere
ostacolati da meccanismi poco trasparenti o incoerenti.
