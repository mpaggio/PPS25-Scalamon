package scalamon.logics.state

trait PokemonStateModule:
  type PokemonState

  def pokemonState(hp: Int): PokemonState

  extension (ps: PokemonState)
    def hp: Int
    def damage(amount: Int): PokemonState
    def heal(amount: Int): PokemonState


object PokemonStateModuleImpl extends PokemonStateModule:
  override type PokemonState = Int

  def pokemonState(hp: Int): PokemonState = hp

  extension (ps: PokemonState)
    infix def hp: Int = ps
    infix def damage(amount: Int): PokemonState = pokemonState(ps.hp - amount)
    infix def heal(amount: Int): PokemonState = pokemonState(ps.hp + amount)