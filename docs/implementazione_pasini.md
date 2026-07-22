# Implementazione

## Contributo generale

Il mio contributo nel progetto è stato principalmente nella definizione dello stato e nell'ideazione dell'architettura
funzionale che lo aggiorna. Come implementazione mi sono occupato dei moduli con i modificatori di stato per la
definizione degli effetti, degli item e del game loop. Ho collaborato anche nell'orchestrazione del turno e nella
definizione del logging system.

## Stato e modificatori

I modificatori dello stato descritti nel design sono stati organizzati in moduli, uno per ogni livello di lifting.
La forma ricorrente è fattorizzata in un unico trait, `StateComponent`,
tramite membri di tipo astratti: ogni modulo dichiara il proprio `State` e il proprio `InnerState`, e ne deriva
gli alias `Op` e `InnerOp` che compaiono in tutte le firme e accorciano notevolmente le firme delle funzioni.

```scala
trait StateComponent:
  protected type State
  protected type Op = State => State
  protected type InnerState
  protected type InnerOp = InnerState => InnerState

trait BattleStateModule extends StateComponent:
  override protected type State = BattleState
  override protected type InnerState = PlayerState

object BattleStateModuleImpl extends BattleStateModule:
  def self(f: InnerOp): Op = bs => bs.copy(self = f(bs.self))
  def opponent(f: InnerOp): Op = bs => bs.copy(opponent = f(bs.opponent))
  def switchSelfOpponent: Op = bs => bs.copy(self = bs.opponent, opponent = bs.self)
```

Ogni modulo fissa la coppia di tipi (qui `State = BattleState`, `InnerState = PlayerState`) e il relativo oggetto
`Impl` fornisce i combinatori concreti, ciascuno una `copy` che ricostruisce il solo campo toccato. Poiché `Op` e
`InnerOp` sono membri di tipo e non parametri generici, tutti i moduli hanno dichiarazioni simili, pur riferendosi
ai propri tipi concreti.

Per scelta gli alias sono trasparenti e non opachi, quindi dove i combinatori non bastano si può passare
una lambda scritta a mano, rendendo esprimibile qualunque effetto custom non previsto dal DSL.
Infine la clausola `export` di Scala 3 raccoglie i modificatori dei vari moduli sotto l'unico namespace di `StateTransformer`,
così che mosse, abilità e item li importino da un solo punto.

### Limitazioni

Una limitazione riscontrata con l'architettura è stata l'impossibilità di ispezionare un'operazione nel dettaglio,
ad esempio stabilire se una mossa modifichi i punti salute. 
Dato un `Op` arbitrario, la funzione catturata nella chiusura non è recuperabile, 
e non si può decidere da quale combinatore provenga: una funzione è opaca al pattern matching.

```scala
object active:
  def apply(f: InnerOp): Op = ps => ps.copy(team = ps.team.updated(ps.activeId, f(ps.getActive)))
  def unapply(op: Op): Option[InnerOp] = ???  // non implementabile
```
L'introspezione richiederebbe di reificare gli effetti come tipo di dato (una ADT di comandi),
rinunciando però alla componibilità e all'estensibilità aperta di `State => State`: un compromesso accettato con la
rappresentazione funzionale.

## Item a durata limitata

Alcuni item hanno un effetto non istantaneo. Per descrivere in un unico punto l'intera specifica di un item,
la case class `Item` (che è un `Action`, quindi uno `StateTransformer`) aggiunge due campi opzionali:
`until`, l'insieme dei trigger che ne annullano l'effetto, e `onCancel`, la trasformazione da applicare all'annullamento.
I default (`Set.empty` e `identity`) rendono il meccanismo di durata interamente opzionale:
un item istantaneo si dichiara senza menzionarlo.

```scala
case class Item(
    name: String,
    description: String,
    effect: StateTransformer,
    until: Set[AbilityTrigger] = Set.empty,
    onCancel: StateTransformer = identity
) extends Action:

  def apply(bs: BattleState): BattleState =
    if bs.self.items.contains(this) then
      val onTrigger = onCancel andThen removePassiveEffect(name) andThen updateLogs(logItemRunsOut(bs.self, this))
      val addCancel = addPassiveEffect(name, t => if until.contains(t) then onTrigger else identity)
      val consumeItem = self(items(_ - this))
      val logItemUse = updateLogs(logUseItem(bs.self, this))
      (effect andThen addCancel andThen consumeItem andThen logItemUse)(bs)
    else
      updateLogs(logError(s"Item $name not found"))(bs)
```

Come conseguenza dei moduli dello stato, l'intera logica — effetto, registrazione della scadenza, consumo dalla scorta, logging — 
è una composizione di `StateTransformer` con `andThen`. 
Anche le operazioni di servizio (rimuovere l'item, scrivere sul log) e quelle di meta-livello
(registrare o rimuovere un effetto passivo) sono trasformazioni componibili tra loro.
La scadenza è ottenuta registrando come effetto passivo, con chiave `name`, la funzione
 a ogni trigger del turno, se appartiene a `until` scatta `onTrigger`, altrimenti nulla.
`onTrigger` applica `onCancel`, rimuove sé stesso dalle passive (`removePassiveEffect(name)`) e ne registra il log,
così che l'effetto a durata limitata si esaurisca esattamente una volta, gestendo autonomamente il proprio ciclo di vita.
Essendo tutti gli item a uso singolo, il consumo dall'inventario è automatico.
Il risultato è una definizione dichiarativa e autocontenuta:

```scala
Item(
    name = "x_attack",
    description = "Raise Attack until switch out or KO",
    effect   = self(active(modifyStats(attack(increase(1))))),
    until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
    onCancel = self(active(modifyStats(attack(decrease(1)))))
)
```

## Game loop monadico

I due combinatori che il design introduce, `onFirst`/`onSecond`, sono realizzati come extension method sulla
`StateMonad`, generici nel tipo dell'altra metà della coppia:

```scala
extension [S, A](m: StateMonad[S, A])
  def onFirst[S2]:  StateMonad[(S, S2), A] = StateMonad: (s, other) =>
    val (newS, a) = m.run(s); ((newS, other), a)
  def onSecond[S1]: StateMonad[(S1, S), A] = StateMonad: (other, s) =>
    val (newS, a) = m.run(s); ((other, newS), a)
```

Nel tipo del loop, `view.V` è un tipo dipendente dal percorso (path-dependent type):
il tipo di stato opaco è un membro dell'istanza `view` della porta, quindi il tipo di stato del loop dipende
dalla specifica vista pur restando astratto.

```scala
private type Game[A] = StateMonad[(BattleState, view.V), A]
```

Il punto di innesto con la simulazione sfrutta il fatto che `runTurn` restituisce la tupla `(BattleState, TurnResult)`,
che corrisponde con la forma della funzione di run di una `StateMonad[BattleState, TurnResult]`, 
l'esito del turno si incapsula quindi nella monade senza alcun adattamento, e `onFirst` lo solleva su `Game`.

```scala
private def resolveTurn(orchestrator: BattleOrchestrator, choices: TurnChoices): Game[TurnResult] = for
    result <- StateMonad(orchestrator.runTurn(_, choices, speedOf)).onFirst
    _      <- refreshView
  yield result
```

Il loop è infine una funzione ricorsiva sulla `Game`: la for-comprehension rappresenta la sequenza imperativa
e la ricorsione costruisce il turno successivo come valore monadico anziché come chiamata immediata.

```scala
private def gameLoop(orchestrator: BattleOrchestrator): Game[Unit] = for
    choices <- hotSeatChoices
    result  <- resolveTurn(orchestrator, choices)
    _       <- handleForcedSwitch(orchestrator, result)
    _       <- result match
      case Victory(winnerName) => endGame(winnerName)
      case _                   => gameLoop(orchestrator)
  yield ()
```
