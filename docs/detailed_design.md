<!--
## State transformer
- Stato immutabile
- type StateTransofrmer = BattleState => BattleState
- stato diviso in componenti con gerarchia di aggregazione
- un metodo prende una modifica su uno stato interno e restituiscono una funzione sullo stato corrente che applica la modifica ad uno specifico componente
    in questo modo si possono comporre le modifiche in una catena di funzioni che trasformano lo stato corrente in uno stato finale

- Mosse, Abilità e tutti gli effetti presenti nel gioco implementano uno StateTransformer descritti come un innesto dei metodi granulari generali
Esempio:
- weaknessMove: Move = opponent ( active ( modifyStats ( attack( decrease( 5)))))
  healMove: Move = self (allThat(ps => ps.currentHp < 40)( currentHp( Increase( 10))))


## Logging system
Problema: come mostrare all'utente l'ordine di esecuzione delle azioni, le attivazioni delle abilità e tutti gli eventi avvenuti nel turno in modo ordinato?

è stato aggiunto allo stato un istanza di logger che espone metodi specifici per segnalare i diversi tipi di evento.
le implementazioni di mosse, weather, status... oltre a produrre l'effetto voluto segnalano al logger l'evento avvenuto.
Il quale li salva in una collezione ordinata. questa viene consumata alla fine del turno per mostrare gli eventi accaduti nell'ordine.



## Team Building
Il team di pokemon e la scelta delle mosse per ciascuno possono avvenire in diversi modi:
- Random: il team viene generato casualmente
- Affine: le mosse vengono generate con una determinata distribuzione dei tipi e sulla base del tipo del pokemon
- Manuale: la scelta del team è affidata alla UI: l'utente prima sceglie i pokemon e poi le mosse da abbinare a ciascuno

è stato usato il template method pattern: in base alla scelta dell'utente all'inizio della partita istanzia la logica di costruzione relativa



## Monad State loop
Il progetto vuole essere completamente funzionale, come modificare lo stato e la view in loop usando solo istanze immutabili?
è stato usato il pattern state monad all'interno di un game loop ricorsivo
-->



## Design di dettaglio

Il design di dettaglio raffina l'architettura attorno a un'unica idea portante:
**ogni evento di gioco è una funzione pura su uno stato immutabile**.
Le quattro scelte descritte di seguito — il meccanismo degli state transformer, il sistema di logging, il team building e il game loop monadico — sono declinazioni di questo principio a livelli diversi del sistema.

### State transformer e gerarchia dello stato

Lo stato di battaglia è un valore immutabile organizzato per aggregazione gerarchica:
il `BattleState` contiene i due `PlayerState`, ciascuno dei quali contiene i `PokemonState` del team,
che a loro volta aggregano statistiche modificate (`StatsState`) e stato delle mosse (`MoveState`).
Ognuno di questi componenti rappresenta uno stato dinamico variabile in battaglia.
Ad esempio le mosse devono tenere traccia dei PP rimanenti, esistono inoltre effetti che possono alterare le statistiche di un Pokèmon.

```scala
type StateTransformer = BattleState => BattleState
```
Il problema è come esprimere una modifica *profonda* allo stato (es. "ridurre l'attacco del Pokémon attivo dell'avversario")
senza propagare manualmente la ricostruzione di tutti i livelli intermedi?
La soluzione adottata è un insieme di combinatori di modificatori:
ogni modulo di stato espone metodi che prendono una funzione su un proprio componente interno e
restituiscono una funzione sul componente corrente che la applica localmente,
ricostruendo solo il percorso interessato.
La composizione per annidamento dei combinatori che naviga la gerarchia risulta leggibile come un piccolo DSL:

```scala
val weaknessMove = opponent(active(modifyStats(attack(decrease(5)))))
val healMove     = self(allThat(_.currentHp < 40)(currentHp(increase(10))))
```

I selettori del *PlayerState* `self` e `opponent` permettono di descrivere se l'effetto
si applica al giocatore che lo invoca o all'avversario.

La conseguenza di design più importante è l'**uniformità**: mosse, abilità, strumenti,
status alterati ed effetti meteo implementano tutti lo stesso contratto `StateTransformer`,
descritti come innesto dei combinatori granulari.
Questo abilita due livelli di composizione: `andThen` per costruire un singolo effetto complesso
da micro-modifiche, e la concatenazione di liste di trasformazioni per la sequenza del turno.

L'orchestratore (`BattleOrchestrator`) raccoglie infatti le trasformazioni delle tre fasi — `startTurn`,
azioni ordinate per priorità e velocità, `endTurn` — e le applica con un `foldLeft` sullo stato iniziale,
producendo il nuovo stato.

### Logging system

Il problema: come mostrare all'utente l'andamento del turno e gli eventi verificatisi
— ordine di esecuzione delle azioni, attivazione delle abilità, danni da status —
se tutta la logica è composta da funzioni pure che non hanno *side effect*?

La soluzione è stata realizzata tramite registrazione degli eventi in un accumulatore di informazioni
e la loro presentazione al termine del turno.
Una volta selezionate le azioni non è più possibile influenzare tramite input utente lo svolgimento del turno.
Questo rende il risultato equivalente ad una soluzione che adotta log concomitanti all'evento in un sistema mutabile.
Il `BattleState` trasporta un'istanza di logger (a sua volta un tipo opaco immutabile) che espone costruttori
specifici per ogni categoria di evento — uso di un oggetto, mossa fallita, status inflitto,
attivazione di abilità. Le implementazioni di mosse, meteo e status, oltre a produrre l'effetto sullo stato,
*decorano* la propria trasformazione con la segnalazione dell'evento tramite `updateLogs`:
la registrazione è quindi essa stessa uno `StateTransformer`, componibile come tutti gli altri.
Poiché gli eventi vengono accodati in una collezione ordinata man mano che il `foldLeft` del turno procede,
l'ordine del log coincide per costruzione con l'ordine di esecuzione.
A fine turno il livello di presentazione consuma la collezione e la mostra all'utente.

*Aggiunta diagramma a stati su BattleState che contiene logger*

### Team building

La costruzione del team ammette tre modalità:
- **Random**: generazione casuale
- **Affine**: mosse STAB più mosse di copertura ricavate dalla tabella dei tipi
- **Manuale**: la scelta di Pokémon, mosse e oggetti è affidata all'utente tramite l'interfaccia.

Le tre modalità differiscono solo nella logica di selezione, non nel come si costruisce uno stato di gioco valido.
È stato quindi adottato il **Template Method pattern**:
il trait `TeamBuilder` definisce l'algoritmo fisso `buildTeam` — selezione,
verifica degli invarianti di composizione (6 Pokémon, 4 mosse ciascuno, dotazione di strumenti),
inizializzazione degli stati e scelta del pokémon attivo —
delegando i tre passi di selezione a step astratti.
Un raffinamento funzionale distingue questa realizzazione dal Template Method classico:
gli step astratti sono dichiarati come **membri di tipo funzione**
(`PokemonSelector`, `MoveSelector`, `ItemSelector`), non come metodi.
Le strategie automatiche li implementano con object singleton, mentre `ManualTeamBuilder`
è una classe istanzibile i cui campi sono la strategia.
Questo abilita l'inversione di dipendenza verso l'interfaccia: la porta `GameView` fornisce i tre selettori
come funzioni differite che, quando invocate dal template durante il setup della battaglia, eseguono
le schermate interattive di scelta. Il dominio non sa che dietro un selettore c'è un utente;
l'interfaccia non conosce gli invarianti di composizione,
che restano verificati in un unico punto(`buildTeam`) qualunque sia la strategia.

### Monad state loop

Il progetto vuole essere interamente funzionale: come far evolvere in loop due stati
— quello di battaglia e quello della view — usando solo istanze
immutabili e senza variabili mutabili condivise?
La soluzione prevede il pattern **State monad**: una computazione con stato è un valore
`StateMonad[S, A] = S => (S, A)`, componibile con `map`/`flatMap` e quindi con le for-comprehension.
Il game loop è una **funzione ricorsiva** che restituisce la computazione del turno successivo
finché il `TurnResult` non è una vittoria.


La scelta di dettaglio caratterizzante è lo **stato composto** `(BattleState, view.V)`:
battaglia e vista evolvono nella stessa catena monadica, ma restano incapsulati.
Due metodi (`onFirst`/`onSecond`) permettono di esprimere una computazione su una sola metà della coppia
trasformandola una computazione sul tutto — 
così ogni passo del loop dichiara esplicitamente quale porzione di stato tocca.
Poiché la vista è astratta dietro la porta `GameView` con tipo di stato opaco `V`,
il loop è identico per Swing o per un ipotetico terminale.
Il pattern porta anche l'hot-seat a costo quasi nullo:
la prospettiva del secondo giocatore si ottiene incorniciando
`playerAction` tra due `switchSelfOpponent` (`asOpponent`),
cioè componendo altre due modifiche pure nella stessa catena.

