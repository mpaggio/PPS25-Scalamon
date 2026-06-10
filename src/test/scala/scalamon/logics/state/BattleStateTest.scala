package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.StatsStateModuleImpl.*

class BattleStateTest extends AnyWordSpec with Matchers with StateFixtures:
  "A BattleState" should:

    "initialize with correct players and pokemons" in:
      battle.user.team("Charmander").currentHp shouldEqual 39
      battle.enemy.team("Squirtle").currentHp shouldEqual 44

    "correctly update active enemy pokemon" in:
      type Move = BattleState => BattleState
      val attackMove: Move = _ enemy (_ active (_ damage 10))
      val newState = attackMove(battle)
      newState.enemy.team("Squirtle").currentHp shouldEqual 34

    "correctly update user bench pokemon" in:
      type Move = BattleState => BattleState
      val masochistMove: Move = _ user (_ bench (_ damage 5))
      val newState = masochistMove(battle)
      newState.user.team("Charmander").currentHp shouldEqual 39
      newState.user.team("Bulbasaur").currentHp shouldEqual 40

    "apply move conditionally to user team" in:
      type Move = BattleState => BattleState
      val healAllMove: Move = _ user (_.allThat(ps => ps.currentHp < 40)(_ heal 10))
      // Charmander is 39 so it gets healed, Bulbasaur is 45 so it is skipped.
      val newState = healAllMove(battle)
      newState.user.team("Charmander").currentHp shouldEqual 49
      newState.user.team("Bulbasaur").currentHp shouldEqual 45

    "modify stats to enemy active pokemon" in:
      type Move = BattleState => BattleState
      val weaknessMove: Move = _ enemy (_ active (_ modifyStats (_ attack (_ decrease 5))))
      val newState = weaknessMove(battle)
      newState.enemy.team("Squirtle").modifiedStats.attack.toInt shouldEqual 43

    "chain multiple moves correctly" in:
      type Move = BattleState => BattleState
      val attackMove: Move = _ enemy (_ active (_ damage 10))
      val masochistMove: Move = _ user (_ bench (_ damage 5))
      val healAllMove: Move = _ user (_.allThat(ps => ps.currentHp < 40)(_ heal 10))
      val weaknessMove: Move = _ enemy (_ active (_ modifyStats (_ attack (_ decrease 5))))
      val chainedMove = attackMove andThen masochistMove andThen healAllMove andThen weaknessMove
      val newState = chainedMove(battle)
      // Initial: Enemy active(Squirtle)=44, Atk=48. User active(Charmander)=39, bench(Bulbasaur)=45
      // attack: Enemy active HP -> 34
      // masochist: User bench (Bulbasaur) HP -> 40
      // healAll < 40: User active HP -> 49, User bench HP is 40 (so not <40) -> 40
      // weakness: Enemy active Atk -> 43
      newState.enemy.team("Squirtle").currentHp shouldEqual 34
      newState.user.team("Charmander").currentHp shouldEqual 49
      newState.user.team("Bulbasaur").currentHp shouldEqual 40
      newState.enemy.team("Squirtle").modifiedStats.attack.toInt shouldEqual 43
