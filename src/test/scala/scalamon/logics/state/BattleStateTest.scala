package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.BattleStateModuleImpl.*

class BattleStateTest extends AnyWordSpec with Matchers with BattleFixtures:

  private val rename: PlayerState => PlayerState = p => p.copy(name = "Renamed")

  "A BattleState" should:

    "initialize with the given players and no passive effects" in:
      battle.self shouldEqual player1
      battle.opponent shouldEqual player2
      battle.passiveEffects shouldBe empty

    "apply a modifier to self only" in:
      val b = self(rename)(battle)
      b.self.name shouldEqual "Renamed"
      b.opponent shouldEqual battle.opponent

    "apply a modifier to opponent only" in:
      val b = opponent(rename)(battle)
      b.opponent.name shouldEqual "Renamed"
      b.self shouldEqual battle.self

    "switch self and opponent" in:
      val b = switchSelfOpponent(battle)
      b.self shouldEqual battle.opponent
      b.opponent shouldEqual battle.self
      switchSelfOpponent(b).self shouldEqual battle.self

    "set the weather" in:
      setWeather(alternativeWeather)(battle).weather shouldEqual alternativeWeather

    "accumulate passive effects" in:
      val effect: PassiveEffect = _ => bs => bs
      val b = addPassiveEffect(effect)(addPassiveEffect(effect)(battle))
      b.passiveEffects should have size 2
