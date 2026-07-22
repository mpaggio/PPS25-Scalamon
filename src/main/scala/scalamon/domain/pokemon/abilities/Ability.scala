package scalamon.domain.pokemon.abilities

/**
 * Represents the different abilities that a Pokémon can have.
 * Each ability has a unique effect on the battle state, which is defined in the AbilityDSL.
 */
enum Ability:
  // Fire-Type
  case Blaze, SolarScales, SolarPower, Drought, FlashFire, DroughtAura, FlameBody, RunAway, Guts
  // Water-Type
  case Torrent, RainDish, WaterAbsorb, Hydration, Intimidate, Moxie
  // Grass-Type
  case Overgrow, Chlorophyll, ThickFat, EffectSpore, Regenerator
  // Electric-Type
  case Static, LightningRodLite, LightningRod, SurgeSurfer, Aftermath, VoltAbsorb, QuickFeet
  // Psychic-Type
  case Synchronize, MagicGuard, Forewarn, DrySkin, Pressure, CloudNine, SwiftSwim
  // Poison-Type
  case ShedSkin, PoisonTouch, Levitate, CursedBody, ShadowTag, LiquidOoze
  