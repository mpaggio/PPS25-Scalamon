package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.moves.DamageMoveCategory.*
import scalamon.domain.moves.MoveDSL.move
import scalamon.domain.pokemon.pokedex.MyPokedex
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.domain.pokemon.statistics.Stats
import scalamon.domain.types.Type.*
import scalamon.logics.state.BattleStateImpl.battleState
import scalamon.logics.state.DamageMoveCalculatorImpl.getDamage
import scalamon.logics.state.PlayerStateModuleImpl.playerState
import scalamon.logics.state.PokemonStateModuleImpl.pokemonInitialState
import scalamon.logics.state.StatsStateModuleImpl.*

class DamageMoveCalculatorTest extends AnyFunSuite:
  val baseStats = Stats(
    hp = fromInt(100),
    attack = fromInt(50),
    defense = fromInt(30),
    specialAttack = fromInt(40),
    specialDefense = fromInt(20),
    speed = fromInt(60)
  )

  val attackerSpecies = MyPokedex.allPokemons.find(_.name == "Charmander").get
  val defenderSpecies = MyPokedex.allPokemons.find(_.name == "Bulbasaur").get

  val attackerPokemonState = pokemonInitialState(attackerSpecies)
  val defenderPokemonState = pokemonInitialState(defenderSpecies)

  val playerAtk = playerState(
    team = Map("Charmander" -> attackerPokemonState),
    active = "Charmander"
  )
  val playerDef = playerState(
    team = Map("Bulbasaur" -> defenderPokemonState),
    active = "Bulbasaur"
  )

  val state = battleState(playerAtk, playerDef)

  val physicalMove = move named "Tackle" withPower 40 withPP 35 withAccuracy 100 withType Normal as Physical
  val specialMove = move named "Flamethrower" withPower 90 withPP 15 withAccuracy 100 withType Fire as Special
  val stabMove = move named "Ember" withPower 40 withPP 25 withAccuracy 100 withType Fire as Special
  val superMove = move named "Surf" withPower 90 withPP 15 withAccuracy 100 withType Water as Special
  val notVeryEffectiveMove = move named "Vine Whip" withPower 45 withPP 25 withAccuracy 100 withType Grass as Special
  val noEffectMove = move named "Thunder Wave" withPower 1 withPP 20 withAccuracy 90 withType Electric as Special

  test("getDamage should return a positive value with valid inputs"){
    val damage = getDamage(state, physicalMove)
    assert(damage > 0, s"Expected damage to be positive, but got $damage")
  }

  test("getDamage should increase if attacker's attack stat increases (Physical)"){
    val boostedPlayerAtk = playerState(
      team = Map("Charmander" -> (attackerPokemonState modifyStats (ss => StatsStateModuleImpl.attack(ss)(_ increase 50)))),
      active = "Charmander"
    )
    val stateNormal = battleState(playerAtk, playerDef)
    val stateBoosted = battleState(boostedPlayerAtk, playerDef)

    val damageNormal = getDamage(stateNormal, physicalMove)
    val damageBoosted = getDamage(stateBoosted, physicalMove)

    assert(damageBoosted > damageNormal,
      s"Expected boosted damage ($damageBoosted) > normal damage ($damageNormal)")
  }

  test("getDamage should decrease if defender's defense stat increases (Physical)") {
    val tankPlayerDef = playerState(
      team = Map("Bulbasaur" -> (defenderPokemonState modifyStats (ss => StatsStateModuleImpl.defense(ss)(_ increase 50)))),
      active = "Bulbasaur"
    )
    val stateNormal = battleState(playerAtk, playerDef)
    val stateTank = battleState(playerAtk, tankPlayerDef)

    val damageNormal = getDamage(stateNormal, physicalMove)
    val damageTank = getDamage(stateTank, physicalMove)

    assert(damageTank < damageNormal,
      s"Expected boosted damage ($damageTank) < normal damage ($damageNormal)")
  }
