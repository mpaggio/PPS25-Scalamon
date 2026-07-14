package scalamon.app

/**
 * Single source of truth for the game's numeric parameters.
 * The number of players is fixed to two by design
 */
object GameConfig:
  val TeamSize: Int = 6
  val MovesPerPokemon: Int = 4
  val ItemsPerPlayer: Int = 4

/** Battle difficulty, replaces the raw strings used previously. */
enum Difficulty:
  case Easy, Medium, Hard

/** Team building mode, replaces the raw strings used previously. */
enum Mode:
  case Manual, Random, Affine
