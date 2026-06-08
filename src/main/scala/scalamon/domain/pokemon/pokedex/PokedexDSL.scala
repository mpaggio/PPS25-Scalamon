package scalamon.domain.pokemon.pokedex

import scalamon.domain.pokemon.abilities.{Ability, AbilitySlot}
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.domain.pokemon.statistics.Stats
import scalamon.domain.pokemon.{PokedexADT, Pokemon}
import scalamon.domain.types.Type
object PokedexDSL:

  def Pokedex(categories: List[Pokemon]*): List[Pokemon] =
    categories.flatten.toList

  def Category(pokemonType: Type)(pokemons: (Type ?=> Pokemon)*): List[Pokemon] =
    given Type = pokemonType
    pokemons.map(builderFunction => builderFunction).toList

  extension (name: String)
    infix def id(pokedexId: Int): PokemonBuilderStep1 =
      PokemonBuilderStep1(name, pokedexId)

  case class PokemonBuilderStep1(name: String, id: Int):
    infix def stats(hp: Int, attack: Int, defense: Int, specialAttack: Int, specialDefense: Int, speed: Int): PokemonBuilderStep2 =
      val s = Stats(
        fromInt(hp), fromInt(attack), fromInt(defense),
        fromInt(specialAttack), fromInt(specialDefense), fromInt(speed)
      )
      PokemonBuilderStep2(name, id, s)

  case class PokemonBuilderStep2(name: String, id: Int, stats: Stats):
    infix def ability(primaryAbility: Ability)(using pokemonType: Type): Pokemon =
      Pokemon(
        pokedexId = PokedexADT.fromInt(id),
        name = name,
        pokemonType = pokemonType,
        baseStats = stats,
        abilitySlot = AbilitySlot(primaryAbility)
      )

  extension (p: Pokemon)

    infix def withSecondaryAbility(secondaryAbility: Ability): Pokemon =
      p.copy(abilitySlot = p.abilitySlot.copy(secondary = Some(secondaryAbility)))

    infix def withHiddenAbility(hiddenAbility: Ability): Pokemon =
      p.copy(abilitySlot = p.abilitySlot.copy(hidden = Some(hiddenAbility)))