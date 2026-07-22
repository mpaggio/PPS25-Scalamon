package scalamon.controller

import scalamon.logics.state.BattleStateModuleImpl.BattleState
import scalamon.logics.state.PlayerStateModuleImpl.PlayerState
import scalamon.logics.state.PokemonStateModuleImpl.PokemonState
import scalamon.logics.turns.Side
import scalamon.logics.turns.Side.*

/**
 * Pure formatting of domain data into plain text for the views.
 * No UI-specific markup lives here: HTML decorations (tooltips, buttons)
 * belong to the Swing view.
 */
object Presenter:

  val playerNames: (String, String) = ("Player 1", "Player 2")

  def status(bs: BattleState): String =
    s"${bs.self.getActive.species.name}" +
      s" HP:${bs.self.getActive.currentHp}/${bs.self.getActive.species.baseStats.hp}" +
      s" vs ${bs.opponent.getActive.species.name}" +
      s" HP:${bs.opponent.getActive.currentHp}/${bs.opponent.getActive.species.baseStats.hp}"

  def weather(bs: BattleState): String =
    s"Current Weather: ${bs.weather}"

  def moveSlots(ps: PokemonState): List[MoveSlot] =
    ps.moves
      .take(GameConfig.MovesPerPokemon)
      .map((name, m) => MoveSlot(name, m.currentPp, m.maxPp))
      .toList

  def forcedSwitchMessage(side: Side): String = s"${sideLabel(side)}: Pokemon KO, select a substitute [mandatory]:"

  def initialSetupLog(mode: Mode, bs: BattleState): String =
    s"--- INITIAL SETUP ---\n" +
      s"Selected mode: $mode\n\n" +
      team(s"TEAM ${bs.self.name.toUpperCase}", bs.self) + "\n\n" +
      team(s"TEAM ${bs.opponent.name.toUpperCase}", bs.opponent) + "\n\n" +
      s"Initial lead: ${bs.self.getActive.species.name} vs ${bs.opponent.getActive.species.name}\n" +
      s"-----------------------\n\n"

  private def team(title: String, ps: PlayerState): String =
    val details = ps.team
      .map((name, pokemon) => s"- $name -> ${pokemon.moves.keys.mkString(", ")}")
      .mkString("\n")
    s"$title:\n$details"

  private def sideLabel(side: Side): String = side match
    case Self => playerNames._1
    case _ => playerNames._2
