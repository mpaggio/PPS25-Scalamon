package scalamon.logics.state

trait AlteratedStatusModule:
  type AlteratedStatus
  type PokemonState
  def alteratedStatus(
                       name: String,
                       update: AlteratedStatus => List[AlteratedStatus], 
                       effect: PokemonState => PokemonState
                     ): AlteratedStatus
  
object AlteratedStatusModuleImpl extends AlteratedStatusModule:
  case class As(
                 name: String,
                 update: AlteratedStatus => List[AlteratedStatus],
                 effect: PokemonState => PokemonState
               )
  
  override type AlteratedStatus = As
  override type PokemonState = PokemonStateModuleImpl.PokemonState

  def alteratedStatus(
                       name: String,
                       update: AlteratedStatus => List[AlteratedStatus], 
                       effect: PokemonState => PokemonState
                     ): AlteratedStatus = As(name, update, effect)
  