package scalamon.logics

import scalamon.logics.state.*
import BattleStateImpl.*
import PlayerStateModuleImpl.*
import PokemonStateModuleImpl.*

type Move = BattleState => BattleState

object BattleEngine:

  def resolve(battleState: BattleState, playerActions: (Move, Move)): BattleState =
    val moves = List(
      playerActions._1,
      playerActions._2,
    )
    battleState

    /*val finalExecutors = battleState.passiveEffects.foldLeft(moves):
      (move, modifier) => move.flatMap(modifier)*/