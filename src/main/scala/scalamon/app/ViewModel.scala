package scalamon.app

import scalamon.util.StateMonad
import scalamon.logics.teambuilder.TeamBuilder.*

/** A move as shown to the player (name and PP), free of any UI markup. */
final case class MoveSlot(name: String, currentPp: Int, maxPp: Int)

/** An item as shown to the player. */
final case class ItemSlot(name: String, description: String)

/** Everything the view needs in order to ask the current player for an action. */
final case class ActionPrompt(
                               moves: List[MoveSlot],
                               switchable: List[String],
                               items: List[ItemSlot]
                             )

/** Snapshot of the battle for rendering purposes. `log` is the text to append to the battle log. */
final case class BattleViewModel(
                                  status: String,
                                  weather: String,
                                  log: String,
                                  moves: List[MoveSlot]
                                )

/**
 * What the player decided to do, expressed in UI-agnostic terms.
 * The application layer translates it into a domain BattleAction.
 */
enum PlayerIntent:
  case Attack(moveName: String)
  case Switch(pokemonName: String)
  case Item(itemName: String)

/**
 * Port of the application towards any user interface (Swing, terminal, ...).
 *
 * The abstract type V is the view's own state, threaded through the
 * State monad. Every method blocks until the player has provided the
 * requested input (where input is requested).
 */
trait GameView:
  type V

  /** The initial, empty view. */
  def initial: V

  def chooseDifficulty: StateMonad[V, Difficulty]
  def chooseMode: StateMonad[V, Mode]

  /** A selector that interactively lets the player pick the team. */
  def chooseTeam(player: String): StateMonad[V, PokemonSelector]

  /** A selector that interactively lets the player pick the moves of a Pokémon. */
  def chooseMoves(player: String): StateMonad[V, MoveSelector]

  /** A selector that interactively lets the player pick the items. */
  def chooseItems(player: String): StateMonad[V, ItemSelector]

  /** Builds the battle screen and shows the initial setup log. */
  def showBattleScreen(vm: BattleViewModel, setupLog: String): StateMonad[V, Unit]

  /** Refreshes status, weather and appends `vm.log` to the battle log. */
  def renderBattle(vm: BattleViewModel): StateMonad[V, Unit]

  /** Asks the current player for an action; retries internally until a valid choice is made. */
  def askAction(prompt: ActionPrompt): StateMonad[V, PlayerIntent]

  /** Asks for a mandatory switch; always returns one of the candidates. */
  def askForcedSwitch(message: String, candidates: List[String]): StateMonad[V, String]

  /** Shows a blocking message to the players (hot-seat transitions, victory, ...). */
  def announce(message: String): StateMonad[V, Unit]

  def close: StateMonad[V, Unit]
