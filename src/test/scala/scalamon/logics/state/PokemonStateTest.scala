package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.PokemonStateModuleImpl.*

class PokemonStateTest extends AnyWordSpec with Matchers with PokemonFixtures:

  "A PokemonState" should:

    "initialize hp, stats, moves and no status" in:
      myPokemon.currentHp shouldEqual 39
      myPokemon.maxHp shouldEqual 39
      myPokemon.modifiedStats.attack shouldEqual 52
      myPokemon.moves.keySet shouldEqual myPokemonMoves.keySet
      myPokemon.statusCondition shouldBe None

    "update current hp" in:
      currentHp(_ => 10)(myPokemon).currentHp shouldEqual 10

    "clamp current hp between 0 and maxHp" in:
      currentHp(_ - 100)(myPokemon).currentHp shouldEqual 0
      currentHp(_ + 100)(myPokemon).currentHp shouldEqual myPokemon.maxHp

    "take damage and heal through the dedicated ops" in:
      val damaged = takeDamage(10)(myPokemon)
      damaged.currentHp shouldEqual 29
      heal(5)(damaged).currentHp shouldEqual 34
      takeDamage(100)(myPokemon).currentHp shouldEqual 0
      heal(100)(damaged).currentHp shouldEqual myPokemon.maxHp

    "apply stat modifiers to modifiedStats only" in:
      val weakened = modifyStats(ss => ss.copy(attack = ss.attack - 5))(myPokemon)
      weakened.modifiedStats.attack shouldEqual 47
      weakened.species.baseStats.attack shouldEqual 52

    "add a status and expose it as status condition" in:
      addStatus(statusA)(myPokemon).statusCondition shouldEqual Some(statusA)

    "replace a status of the same kind instead of duplicating it" in:
      val s = addStatus(statusA)(addStatus(statusA)(myPokemon))
      s.status.count(_.ordinal == statusA.ordinal) shouldEqual 1

    "remove a status" in:
      removeStatus(statusA)(addStatus(statusA)(myPokemon)).statusCondition shouldBe None

    "clear every status condition" in:
      val afflicted = addStatus(statusB)(addStatus(statusA)(myPokemon))
      afflicted.clearStatusCondition.statusCondition shouldBe None

    "apply a transformer to every move" in:
      val used = moves(m => m.copy(currentPp = m.currentPp - 1))(myPokemon)
      used.moves.values.foreach(m => m.currentPp shouldEqual m.maxPp - 1)

    "update a single move by name, leaving the others untouched" in:
      val name = myPokemonMoves.keySet.head
      val updated = updateMove(name)(m => m.copy(currentPp = 0))(myPokemon)
      updated.moveState(name).currentPp shouldEqual 0
      (updated.moves - name) shouldEqual (myPokemon.moves - name)
