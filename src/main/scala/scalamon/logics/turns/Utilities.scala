package scalamon.logics.turns

import scalamon.database.{MoveDatabase, MyAbilityBook}
import scalamon.domain.moves.Accuracy.given
import MoveDatabase.findByName
import scalamon.domain.types.Type
import scalamon.domain.alteredStatus.AlteredStatusModule.{canMove, isSelfHitting}
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.turns.TurnResolutionImpl.isKnockedOut

object Utilities:
  /** Runs f from the perspective of the given side, restoring orientation. */
  def onSide(side: Side)(f: StateTransformer): StateTransformer =
    side match
      case Side.Self => f
      case Side.Opponent => s => switchSelfOpponent(f(switchSelfOpponent(s)))

  /** Runs f from the opponent's perspective, restoring orientation. */
  def asOpponent(f: StateTransformer): StateTransformer = onSide(Side.Opponent)(f)

  /** Runs f once per side, each time from that side's perspective. */
  def forBothSides(f: StateTransformer): StateTransformer =
    state => List(Side.Self, Side.Opponent).foldLeft(state)((s, side) => onSide(side)(f)(s))
  
  def findMove(moveRef: MoveRef): Option[Move] = MoveDatabase.allMoves.findByName(moveRef.value)

  def hasPpFor(pokemon: PokemonState, move: Move): Boolean = pokemon.moveState(move.name).currentPp > 0

  def canMove(pokemon: PokemonState, moveType: Type, currentWeather: Weather): Boolean =
    pokemon.statusCondition.forall(_.canMove(moveType, currentWeather))

  def isSelfHitting(pokemon: PokemonState): Boolean = pokemon.statusCondition.exists(_.isSelfHitting)

  def applyPassiveEffects(trigger: Trigger)(bs: BattleState): BattleState =
    val newBs = MyAbilityBook.runTrigger(trigger, bs.self.getActive.species.abilitySlot)(bs)
    newBs.passiveEffects.foldLeft(newBs)((state, effect) => effect(trigger)(state))

  def aliveBench(player: PlayerState): List[PokemonRef] =
    player.team.collect {
      case (id, pokemon) if id != player.activeId && !isKnockedOut(pokemon) => PokemonRef(id)
    }.toList

  /** Projects the player of the given side out of the battle state. */
  def playerOf(state: BattleState, side: Side): PlayerState = side match
    case Side.Self => state.self
    case Side.Opponent => state.opponent