package scalamon.view

import scalamon.domain.actions.Item
import scalamon.domain.moves.{DamageMove, Move, StatusMove}
import scalamon.domain.pokemon.Pokemon

private [view] object HtmlTooltips:
  
  def pokemonTooltip(pokemon: Pokemon): String =
    s"""<html>
       |<b>${pokemon.name}</b><br/>
       |Type: ${pokemon.pokemonType}<br/>
       |Stats: ${pokemon.baseStats}<br/>
       |Ability: ${pokemon.abilitySlot}
       |</html>""".stripMargin

  def moveTooltip(move: Move): String = move match
    case damage: DamageMove =>
      s"""<html>
         |<b>${damage.name}</b><br/>
         |Type: ${damage.moveType}<br/>
         |Category: ${damage.category}<br/>
         |Power: ${damage.power}<br/>
         |Accuracy: ${damage.accuracy}<br/>
         |PP: ${damage.pp}
         |</html>""".stripMargin
    case status: StatusMove =>
      s"""<html>
         |<b>${status.name}</b><br/>
         |Type: ${status.moveType}<br/>
         |Category: ${status.category}<br/>
         |Accuracy: ${status.accuracy}<br/>
         |PP: ${status.pp}
         |</html>""".stripMargin

  def itemTooltip(item: Item): String =
    s"""<html>
       |<b>${item.name}</b><br/>
       |${item.description}
       |</html>""".stripMargin