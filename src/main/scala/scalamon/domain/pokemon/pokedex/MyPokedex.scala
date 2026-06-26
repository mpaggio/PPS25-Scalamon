package scalamon.domain.pokemon.pokedex

import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.abilities.Ability.*
import scalamon.domain.pokemon.pokedex.PokedexDSL.*
import scalamon.domain.types.Type.*

object MyPokedex:

  val allPokemons: List[Pokemon] = Pokedex(

    Category(Fire)(
      "Charmander" id 4 stats(39, 52, 43, 60, 50, 65) ability Blaze withHiddenAbility SolarScales,
      "Charizard" id 6 stats(78, 84, 78, 109, 85, 100) ability SolarPower withHiddenAbility Drought,
      "Ninetales" id 38 stats(73, 76, 75, 81, 100, 100) ability FlashFire withSecondaryAbility DroughtAura,
      "Ponyta" id 77 stats(50, 85, 55, 65, 65, 90) ability FlameBody withHiddenAbility RunAway,
      "Flareon" id 136 stats(65, 130, 60, 95, 110, 65) ability FlashFire withSecondaryAbility Guts
    ),

    Category(Water)(
      "Squirtle" id 7 stats(44, 48, 65, 50, 64, 43) ability Torrent withHiddenAbility Hydration,
      "Blastoise" id 9 stats(79, 63, 100, 85, 105, 78) ability Torrent withSecondaryAbility RainDish,
      "Vaporeon" id 134 stats(130, 65, 60, 110, 65, 65) ability WaterAbsorb withHiddenAbility Hydration,
      "Gyarados" id 130 stats(95, 125, 79, 60, 100, 81) ability Intimidate withSecondaryAbility Moxie,
      "Lapras" id 131 stats(130, 85, 80, 85, 95, 60) ability WaterAbsorb withHiddenAbility Hydration
    ),

    Category(Grass)(
      "Bulbasaur" id 1 stats(45, 49, 49, 65, 65, 45) ability Overgrow withHiddenAbility Chlorophyll,
      "Venusaur" id 3 stats(80, 82, 83, 100, 100, 80) ability Chlorophyll withSecondaryAbility ThickFat,
      "Oddish" id 43 stats(45, 50, 55, 75, 65, 30) ability Chlorophyll withHiddenAbility RunAway,
      "Bellsprout" id 69 stats(50, 75, 35, 70, 30, 40) ability EffectSpore withHiddenAbility Chlorophyll,
      "Tangela" id 114 stats(65, 55, 115, 100, 40, 60) ability EffectSpore withSecondaryAbility Regenerator
    ),

    Category(Electric)(
      "Pikachu" id 25 stats(35, 55, 40, 50, 50, 90) ability Static withHiddenAbility LightningRodLite,
      "Raichu" id 26 stats(60, 90, 55, 90, 80, 110) ability LightningRod withHiddenAbility SurgeSurfer,
      "Voltorb" id 100 stats(40, 30, 50, 55, 55, 100) ability Static withSecondaryAbility Aftermath,
      "Electabuzz" id 125 stats(65, 83, 57, 95, 85, 105) ability Static withSecondaryAbility Insomnia,
      "Jolteon" id 135 stats(65, 65, 60, 110, 95, 130) ability VoltAbsorb withHiddenAbility QuickFeet
    ),

    Category(Psychic)(
      "Alakazam" id 65 stats(55, 50, 45, 135, 95, 120) ability Synchronize withHiddenAbility MagicGuard,
      "Hypno" id 97 stats(85, 73, 70, 73, 115, 67) ability Insomnia withSecondaryAbility Forewarn,
      "Jynx" id 124 stats(65, 50, 35, 115, 95, 95) ability Forewarn withHiddenAbility DrySkin,
      "Mewtwo" id 150 stats(106, 110, 90, 154, 90, 130) ability Pressure withHiddenAbility MagicGuard,
      "Psyduck" id 54 stats(50, 52, 48, 65, 50, 55) ability CloudNine withSecondaryAbility SwiftSwim
    ),

    Category(Poison)(
      "Arbok" id 24 stats(60, 95, 69, 65, 79, 80) ability Intimidate withSecondaryAbility ShedSkin,
      "Grimer" id 88 stats(80, 80, 50, 40, 50, 25) ability LiquidOoze withSecondaryAbility PoisonTouch,
      "Gastly" id 92 stats(30, 35, 30, 100, 35, 80) ability Levitate withHiddenAbility CursedBody,
      "Gengar" id 94 stats(60, 65, 60, 130, 75, 110) ability CursedBody withHiddenAbility ShadowTag,
      "Tentacruel" id 73 stats(80, 70, 65, 80, 120, 100) ability LiquidOoze withSecondaryAbility RainDish
    )
  )