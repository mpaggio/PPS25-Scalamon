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
      battle.self.team("Charmander").currentHp shouldEqual 39
      battle.opponent.team("Squirtle").currentHp shouldEqual 44

    "correctly update active enemy pokemon" in:
      type Move = BattleState => BattleState
      val attackMove: Move = opponent(active(currentHp(decrease(10))))

      val newState = attackMove(battle)
      newState.opponent.team("Squirtle").currentHp shouldEqual 34

    "correctly update user bench pokemon" in:
      type Move = BattleState => BattleState
      val masochistMove: Move = self ( bench ( currentHp (decrease (5))))
      val newState = masochistMove(battle)
      newState.self.team("Charmander").currentHp shouldEqual 39
      newState.self.team("Bulbasaur").currentHp shouldEqual 40

    "apply move conditionally to user team" in:
      type Move = BattleState => BattleState
      val executeMove: Move = self (allThat(ps => ps.currentHp < 40)( currentHp( decrease( 40))))
      val newState = executeMove(battle)
      newState.self.team("Charmander").currentHp shouldEqual 0
      newState.self.team("Bulbasaur").currentHp shouldEqual 45

    "modify stats to enemy active pokemon" in:
      type Move = BattleState => BattleState
      val weaknessMove: Move = opponent ( active ( modifyStats ( attack( decrease( 5)))))
      val newState = weaknessMove(battle)
      newState.opponent.team("Squirtle").modifiedStats.attack shouldEqual 43

    "chain multiple moves correctly" in:
      type Move = BattleState => BattleState
      val attackMove: Move = opponent ( active ( currentHp( decrease(10))))
      val masochistMove: Move = self ( bench ( currentHp( decrease(5))))
      val healAllMove: Move = self ( allThat(ps => ps.currentHp < 40)(currentHp( increase(10))))
      val weaknessMove: Move = opponent ( active ( modifyStats ( attack( decrease(5)))))
      val chainedMove = attackMove andThen masochistMove andThen healAllMove andThen weaknessMove
      val newState = chainedMove(battle)
      // Initial: Enemy active(Squirtle)=44, Atk=48. User active(Charmander)=39, bench(Bulbasaur)=45
      // attack: Enemy active HP -> 34
      // masochist: User bench (Bulbasaur) HP -> 40
      // healAll < 40: User active HP -> 49, User bench HP is 40 (so not <40) -> 40
      // weakness: Enemy active Atk -> 43
      newState.opponent.team("Squirtle").currentHp shouldEqual 34
      newState.self.team("Charmander").currentHp shouldEqual 39
      newState.self.team("Bulbasaur").currentHp shouldEqual 40
      newState.opponent.team("Squirtle").modifiedStats.attack shouldEqual 43
