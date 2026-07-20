# Design di dettaglio

Il design di dettaglio raffina l'architettura attorno a un'unica idea portante:
**ogni evento di gioco è una funzione pura su uno stato immutabile**.
Le quattro scelte descritte di seguito — il meccanismo degli state transformer, il sistema di logging, il team building e il game loop monadico — sono declinazioni di questo principio a livelli diversi del sistema.

## Stato immutabile e State transformer

L'intero stato di battaglia è modellato come un valore immutabile: una modifica non altera
lo stato esistente, ma ne produce una nuova versione. Il vantaggio è duplice.
Da un lato l'evoluzione della partita diventa una sequenza di valori ben definiti,
priva di stati intermedi inconsistenti e di effetti collaterali nascosti;
dall'altro ogni passaggio è riproducibile e testabile in isolamento,
perché il risultato dipende soltanto dallo stato in ingresso.

Lo stato è organizzato per aggregazione gerarchica: il `BattleState` contiene i due `PlayerState`,
ciascuno dei quali contiene i `PokemonState` del team, che a loro volta aggregano statistiche
modificate (`StatsState`) e stato delle mosse (`MoveState`). Ogni componente rappresenta una
porzione dinamica della battaglia: le mosse tengono traccia dei PP rimanenti, le statistiche
possono essere alterate dagli effetti, i Pokémon accumulano status.

Su questo stato agisce una famiglia di effetti estremamente eterogenea: una mossa infligge danno
all'avversario, un'abilità può cambiare il meteo o reagire a un cambio di Pokémon, uno strumento
può rianimare l'intera panchina, uno status logora il proprio portatore a fine turno.
Nessuna interfaccia specifica ("infliggi danno", "cura di N") potrebbe coprirli tutti,
e ogni nuova categoria di effetto ne richiederebbe l'estensione.
L'unico denominatore comune è che ogni effetto trasforma lo stato di battaglia nel suo complesso;
da qui il contratto unico del sistema:

```scala
type StateTransformer = BattleState => BattleState
```

Aggiungere un effetto nuovo e originale significa quindi scrivere una nuova funzione di questo
tipo: il motore di gioco non va modificato, perché sa già comporre ed eseguire `StateTransformer`
arbitrari.

Resta il problema di come esprimere una modifica *profonda* (es. "ridurre l'attacco del Pokémon
attivo dell'avversario") senza ricostruire manualmente tutti i livelli intermedi della gerarchia.
La soluzione è un insieme di **combinatori granulari**: ogni modulo di stato espone metodi che
prendono una funzione su un proprio componente interno e restituiscono una funzione sul componente
corrente, che la applica localmente ricostruendo solo il percorso interessato.
L'annidamento dei combinatori naviga la gerarchia e si legge come un piccolo DSL:

```scala
val weaknessMove = opponent(active(modifyStats(attack(decrease(5)))))
val healMove     = self(allThat(_.currentHp < 40)(currentHp(increase(10))))
```

I selettori del `PlayerState`, `self` e `opponent`, dichiarano se l'effetto si applica al giocatore
che lo invoca o all'avversario.

Il contratto unico abilita infine due livelli di composizione: `andThen` per costruire un singolo
effetto complesso a partire da micro-modifiche, e la concatenazione di liste di trasformazioni per
la sequenza del turno. L'orchestratore (`BattleOrchestrator`) raccoglie infatti le trasformazioni
delle tre fasi — `startTurn`, azioni ordinate per priorità e velocità, `endTurn` — e le applica con
un `foldLeft` sullo stato iniziale, producendo il nuovo stato.

## Logging system

Il problema: come mostrare all'utente l'andamento del turno e gli eventi verificatisi
— ordine di esecuzione delle azioni, attivazione delle abilità, danni da status —
se tutta la logica è composta da funzioni pure, prive di *side effect*?

La soluzione è stata realizzata tramite registrazione degli eventi in un accumulatore di informazioni
e la loro presentazione al termine del turno.
Il `BattleState` trasporta un'istanza di logger che espone costruttori specifici per ogni categoria di evento: mossa fallita, status
inflitto, attivazione di abilità... Le implementazioni di mosse, meteo e status, oltre a produrre
l'effetto sullo stato, *decorano* la propria trasformazione con la segnalazione dell'evento tramite
`updateLogs`: la registrazione è quindi essa stessa uno `StateTransformer`, componibile come tutti
gli altri.

Poiché gli eventi vengono accodati in una collezione ordinata man mano che il turno
procede, l'ordine del log coincide per costruzione con l'ordine di esecuzione. A fine turno il
livello di presentazione consuma la collezione e la mostra all'utente. La presentazione differita
non è percepibile: una volta selezionate le azioni, l'input dell'utente non può più
influenzare lo svolgimento del turno, quindi il risultato è equivalente a quello di un sistema
mutabile che logga ogni evento nel momento in cui accade.

![Diagramma di sequenza del BattleLogger](resources/BattleLoggerSequenceDiagram.png)

## Team building

La costruzione del team ammette tre modalità:

- **Random**: generazione casuale di team e mosse
- **Affine**: mosse dello stesso tipo del Pokémon (STAB) più mosse di copertura ricavate dalla tabella dei tipi
- **Manuale**: la scelta di Pokémon, mosse e strumenti è affidata all'utente tramite l'interfaccia

Le tre modalità differiscono solo nella logica di selezione degli elementi,
non nel come si costruisce uno stato di gioco valido.
È stato quindi adottato il **Template Method pattern**: il trait `TeamBuilder`
definisce l'algoritmo fisso `buildTeam` — selezione, verifica degli invarianti di composizione
(6 Pokémon, 4 mosse ciascuno, dotazione di strumenti), inizializzazione degli stati e scelta del
Pokémon attivo — delegando i tre passi di selezione a step astratti.

Un raffinamento funzionale distingue questa realizzazione dal Template Method classico: gli step
astratti sono dichiarati come **membri di tipo funzione** (`PokemonSelector`, `MoveSelector`,
`ItemSelector`), non come metodi. Le strategie automatiche li implementano come singleton privi di
stato, `ManualTeamBuilder` è invece una classe istanziabile i cui campi sono la strategia: puro dato.

Questa forma abilita l'inversione di dipendenza verso l'interfaccia: la porta `GameView` fornisce i
tre selettori come funzioni differite che, quando invocate dal template durante il setup della
battaglia, eseguono le schermate interattive di scelta. Il dominio non sa che dietro un selettore
c'è un utente; l'interfaccia non conosce gli invarianti di composizione, che restano verificati in
un unico punto (`buildTeam`), qualunque sia la strategia.

## Monad state loop

Il progetto vuole essere interamente funzionale: come far evolvere in loop due stati — quello di
battaglia e quello della view — usando solo istanze immutabili e senza variabili mutabili
condivise?

La soluzione adotta il pattern **State monad**: una computazione con stato è un valore
`StateMonad[S, A] = S => (S, A)`, componibile con `map`/`flatMap` e quindi con le for-comprehension.
Il game loop è una **funzione ricorsiva** che restituisce la computazione del turno successivo
finché il `TurnResult` non è una vittoria.

La scelta di dettaglio caratterizzante è lo **stato composto** `(BattleState, view.V)`: battaglia e
vista evolvono nella stessa catena monadica, ma restano incapsulate. Due combinatori
(`onFirst`/`onSecond`) sollevano una computazione su una sola metà della coppia in una computazione
sul tutto, così ogni passo del loop dichiara esplicitamente quale porzione di stato tocca.
Poiché la vista è astratta dietro la porta `GameView`, con tipo di stato opaco `V`, il loop è
identico per Swing o per un ipotetico terminale.

Il pattern porta anche l'hot-seat a costo quasi nullo: la prospettiva del secondo giocatore si
ottiene incorniciando `playerAction` tra due `switchSelfOpponent` (`asOpponent`), cioè componendo
altre due trasformazioni pure nella stessa catena.
