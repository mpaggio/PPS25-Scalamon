package scalamon.logics.turns

opaque type TrainerId = String
object TrainerId:
  def apply(value: String): TrainerId = value

  extension(trainerId: TrainerId)
    def value: String = trainerId

opaque type PokemonRef = String
object PokemonRef:
  def apply(value: String): PokemonRef = value

  extension(pokemonRef: PokemonRef)
    def value: String = pokemonRef

opaque type MoveRef = String
object MoveRef:
  def apply(value: String): MoveRef = value

  extension(moveRef: MoveRef)
    def value: String = moveRef

enum BattleAction:
  case UseMove(
              trainerId: TrainerId,
              attacking: PokemonRef,
              defending: PokemonRef,
              move: MoveRef,
              priority: Int
              )
  case SwitchPokemon(
                    trainerId: TrainerId,
                    from: PokemonRef,
                    to: PokemonRef,
                    priority: Int
                    )
  