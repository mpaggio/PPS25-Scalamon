# Design architetturale

## Visione d'insieme

Scalamon è un'applicazione monolitica a processo singolo sulla JVM: la modalità di gioco è
l'hot-seat locale, quindi il sistema non comprende componenti distribuiti, servizi di rete né
persistenza, e le scelte architetturali riguardano interamente la struttura interna.

L'architettura è presentata attraverso due viste complementari. La prima (Figura 1) descrive la
**simulazione**: il modello dello stato di battaglia, i contratti che uniformano gli effetti di
gioco e il componente `Turn` che li esegue. La seconda (Figura 2) descrive l'**applicazione**: il
`GameLoop` che governa la partita, la porta verso l'interfaccia utente e la costruzione dello stato
iniziale. Le due viste sono cucite da due cerniere che compaiono in entrambe: `Turn`, che la
simulazione offre e l'applicazione invoca, e `GameLoop`, che la simulazione vede come propria
controparte — il fornitore di azioni e il consumatore di risultati.

Due pattern governano l'insieme. Il primo è un'**architettura a strati con regola delle
dipendenze**: l'applicazione dipende dalla simulazione, la simulazione dai soli contratti e dai
dati di gioco, e nulla dipende verso l'alto. Il secondo è **Ports & Adapters** sul confine con
l'utente: l'applicazione dichiara la porta astratta `GameView` e la vista Swing è un adapter che la
implementa — l'unica dipendenza che risale gli strati, mediata da un contratto. Ne risulta lo
schema *functional core, imperative shell*: simulazione e applicazione sono funzioni pure su valori
immutabili, mentre gli effetti (rendering, input, dialoghi) sono confinati nel guscio Swing.

## Architettura della simulazione (Figura 1)

**Lo stato.** `State` è il valore immutabile che descrive integralmente la battaglia. È una
composizione gerarchica: contiene esattamente due `Player` e la condizione `Weather` corrente;
ogni `Player` possiede sei `Pokemon` e la propria dotazione di `Item`; ogni `Pokemon` aggrega le
proprie statistiche, quattro `Move`, la `Ability` e gli eventuali `AlteredStatus`.
Lo stato trasporta inoltre il log degli eventi del turno, che confluisce nei `Results`.

**I contratti.** Il cuore della vista è la triade di trait che uniforma gli effetti di gioco.
`StateTransformer` è il contratto base: `apply(State): State`, una trasformazione pura dello stato
completo. `Action` — ciò che un giocatore sceglie deliberatamente — *è* uno `StateTransformer`
(generalizzazione): lo realizzano `Move` e `Item`. `PassiveEffect` — ciò che il gioco innesca da
sé — non è invece una trasformazione, ma una *fabbrica* di trasformazioni: `trigger(Event):
StateTransformer` restituisce, per ogni evento del turno (inizio, danno subito, cambio, KO...),
la trasformazione da applicare in quel momento; lo realizzano `Ability`, `AlteredStatus` e
`Weather`. L'asimmetria è deliberata: le azioni si eseguono quando vengono scelte, gli effetti
passivi reagiscono agli `Event` emessi durante l'esecuzione.

**L'esecutore.** `Turn` riceve dallo
strato applicativo le azioni scelte dai giocatori e lo stato corrente, ne determina l'ordine
(priorità dell'azione, poi velocità del Pokémon attivo), emette gli `Event` di turno
raccogliendo dai `PassiveEffect` le trasformazioni innescate, e applica l'intera sequenza di
`StateTransformer` allo stato, restituendo il nuovo stato e i `Results` — l'esito del turno
(partita in corso, cambio obbligato, vittoria) insieme al log ordinato degli eventi accaduti.

**Il confine.** Dal punto di vista della simulazione, `GameLoop` è la controparte astratta che
fornisce le azioni (`chooseAction(): Action`) e consuma i risultati (`showResults(Results)`). La
simulazione non sa se dietro ci sia un'interfaccia grafica, un terminale o un test automatico: le
bastano azioni in ingresso e risultati in uscita.

## Architettura dell'applicazione (Figura 2)

**Il loop.** `GameLoop` espone `run()` e coordina la partita: setup, costruzione dei team, ciclo
dei turni. Opera su `GameState`, la composizione dei due stati che evolvono durante la partita:
il `BattleState` della simulazione e lo stato della vista (`Frame`). Tenerli in un unico valore
composto permette al loop di essere una catena di computazioni pure sull'intero stato di gioco,
in cui ogni passo dichiara quale metà tocca.

**La porta.** `GameView` è il trait che media ogni interazione con l'utente. I dati che lo
attraversano sono deliberatamente semplici: verso la vista viaggiano i `ViewModel` — proiezioni
testuali dello stato (situazione della battaglia, meteo, log, mosse con i PP) e richieste di
input (mosse disponibili, panchina, strumenti) — mentre dalla vista tornano le scelte dell'utente
come intenzioni identificate per nome (attacco, cambio, uso di strumento), che il `GameLoop`
traduce nelle `Action` di dominio da consegnare a `Turn`. La porta è anche il canale del team
building manuale: fornisce i selettori interattivi con cui l'utente compone squadra, mosse e
strumenti.

**L'adapter.** `View` realizza `GameView` con la tecnologia concreta (Swing): costruisce le
schermate, gestisce i dialoghi e converte gli eventi grezzi in intenzioni. Si appoggia a `Frame`,
la facciata sui widget: verso di essa invia comandi di costruzione e aggiornamento
dell'interfaccia, e da essa riceve gli eventi come semplici stringhe (il nome del widget premuto)
attraverso una coda che disaccoppia il thread grafico dal loop di gioco.

**La costruzione.** `TeamBuilder` produce lo stato iniziale della partita: `GameLoop` gli
consegna la strategia di selezione scelta dall'utente (casuale, affine ai tipi, o manuale — nel
qual caso la strategia è fatta dei selettori forniti dalla porta) e riceve un `State` valido per
costruzione, con gli invarianti di composizione verificati in un unico punto.

**Il flusso di un turno.** Componendo le due viste, il percorso dei dati in una iterazione è:
l'input dell'utente diventa evento-stringa in `Frame`, intenzione in `View`, `Action` di dominio
in `GameLoop`, che raccolte le azioni di entrambi i giocatori invoca `Turn`; il nuovo `State` e i
`Results` tornano al `GameLoop`, che ne proietta un `ViewModel` e lo consegna alla porta per il
rendering. Ogni attraversamento di confine cambia il livello di astrazione del dato — stringa,
intenzione, azione, stato, view model — ed è questo a mantenere i componenti sostituibili.

## Scelte tecnologiche cruciali

**Scala 3** è un abilitatore architetturale prima che un linguaggio di implementazione: i trait
con membri astratti realizzano porte e contratti (`GameView`, `StateTransformer`), gli opaque
types incapsulano i tipi di valore del dominio con i loro invarianti, extension methods e metodi
infix sostengono i DSL dichiarativi dei dati di gioco, i parametri contestuali iniettano le
politiche configurabili (difficoltà, meteo, generazione di probabilità) senza accoppiamento
esplicito.

**Scala Swing con facciata e coda bloccante degli eventi** è la scelta che rende compatibile una
GUI event-driven con un loop funzionale sincrono: i listener depositano il nome dell'evento in
una coda, il loop lo preleva con una lettura bloccante. Il modello a callback è invertito in un
modello pull, e l'applicazione resta una sequenza di computazioni monadiche.

**Nessuna libreria funzionale esterna**: la State monad che sostiene il `GameLoop` è
autoprodotta, a beneficio del controllo didattico e dell'assenza di dipendenze, rinunciando a
ottimizzazioni (stack-safety) non necessarie alla scala del progetto. È infrastruttura
trasversale e per questo non compare nei diagrammi come componente.

**Assenza di componenti distribuiti, per scelta e non per vincolo**: il turno è una funzione pura
`(List[Action], State) => (State, Results)` e l'interfaccia sta dietro una porta con dati
serializzabili; un'eventuale evoluzione multiplayer in rete si ridurrebbe a un adapter remoto
della porta e al trasporto di azioni e view model, senza toccare simulazione e loop.
