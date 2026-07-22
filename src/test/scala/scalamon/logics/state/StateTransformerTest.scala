package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.StateTransformerModuleImpl.*

class StateTransformerTest extends AnyWordSpec with Matchers with StateFixtures:

  "A composed StateTransformer" should:

    "damage the enemy active pokemon" in:
      val attackMove: StateTransformer = opponent(active(currentHp(decrease(10))))
      attackMove(battle).opponent.team("Squirtle").currentHp shouldEqual 34

    "damage the user bench" in:
      val masochistMove: StateTransformer = self(bench(currentHp(decrease(5))))
      val b = masochistMove(battle)
      b.self.team("Charmander").currentHp shouldEqual 39
      b.self.team("Bulbasaur").currentHp shouldEqual 40

    "apply conditionally to the user team" in:
      val executeMove: StateTransformer = self(allThat(pk => pk.currentHp < 40)(currentHp(decrease(40))))
      val b = executeMove(battle)
      b.self.team("Charmander").currentHp shouldEqual 0
      b.self.team("Bulbasaur").currentHp shouldEqual 45

    "lower the stats of the enemy active pokemon" in:
      val weaknessMove: StateTransformer = opponent(active(modifyStats(attack(decrease(5)))))
      weaknessMove(battle).opponent.team("Squirtle").modifiedStats.attack shouldEqual 43

    "combine damage with a side switch" in:
      val move: StateTransformer = opponent(active(takeDamage(50))) andThen switchSelfOpponent
      val b = move(battle)
      b.self.name shouldEqual "Player2"
      b.self.team("Squirtle").currentHp shouldEqual 0

    "chain multiple moves" in:
      val attackMove: StateTransformer = opponent(active(currentHp(decrease(10))))
      val masochistMove: StateTransformer = self(bench(currentHp(decrease(5))))
      val healAllMove: StateTransformer = self(allThat(pk => pk.currentHp < 40)(currentHp(increase(10))))
      val weaknessMove: StateTransformer = opponent(active(modifyStats(attack(decrease(5)))))
      val chained = attackMove andThen masochistMove andThen healAllMove andThen weaknessMove
      val b = chained(battle)
      // attack:    Squirtle 44 -> 34
      // masochist: Bulbasaur 45 -> 40
      // healAll:   Charmander 39 -> 39 (maxHp), Bulbasaur 40 -> 40
      // weakness:  Squirtle Atk 48 -> 43
      b.opponent.team("Squirtle").currentHp shouldEqual 34
      b.self.team("Charmander").currentHp shouldEqual 39
      b.self.team("Bulbasaur").currentHp shouldEqual 40
      b.opponent.team("Squirtle").modifiedStats.attack shouldEqual 43

    "be referentially transparent" in:
      val attackMove: StateTransformer = opponent(active(currentHp(decrease(100))))
      attackMove(battle)
      battle.opponent.team("Squirtle").currentHp shouldEqual 44
