package scalamon.gui

import BattleWindowStateImpl.*
import scalamon.logics.state.DamagePolicy
import scalamon.logics.teambuilder.RandomTeamBuilder.RandomTeamBuilder
import scalamon.logics.teambuilder.AffineTeamBuilder.AffineTeamBuilder
import scalamon.logics.teambuilder.TeamBuilder.*

object SetupGUI:

  final case class GameSetup(selectedMode: String, selectedDifficulty: String)

  def damagePolicyFromChoice(choice: String): DamagePolicy = choice match
    case "Easy"   => DamagePolicy.Easy.given_DamagePolicy
    case "Medium" => DamagePolicy.Medium.given_DamagePolicy
    case "Hard"   => DamagePolicy.Hard.given_DamagePolicy
    case _        => DamagePolicy.Medium.given_DamagePolicy

  private def difficultyScreen: State[Window, Unit] = for
    _ <- clear()
    _ <- useMenuCenter()
    _ <- setSize(500, 320)
    _ <- addCenterLabel("SCALAMON", "DifficultyMainTitle")
    _ <- addCenterLabel("Select the difficulty:", "DifficultySubTitle")
    _ <- addCenterButton("Easy", "Easy")
    _ <- addCenterButton("Medium", "Medium")
    _ <- addCenterButton("Hard", "Hard")
    _ <- show()
  yield ()

  private def chooseDifficultyScreen: State[Window, String] = for
    _ <- difficultyScreen
    event <- nextEvent()
  yield event

  private def modeScreen: State[Window, Unit] = for
    _ <- clear()
    _ <- useMenuCenter()
    _ <- setSize(500, 320)
    _ <- addCenterLabel("SCALAMON", "SetupMainTitle")
    _ <- addCenterLabel("Select the team building mode:", "SetupSubTitle")
    _ <- addCenterButton("Manual", "Manual")
    _ <- addCenterButton("Random", "Random")
    _ <- addCenterButton("Affine", "Affine")
    _ <- show()
  yield ()

  private def chooseModeScreen: State[Window, String] = for
    _ <- modeScreen
    event <- nextEvent()
  yield event

  def chooseGameSetup(window: Window): (Window, GameSetup) =
    val (windowAfterDifficulty, selectedDifficulty) = chooseDifficultyScreen.run(window)
    val (windowAfterMode, selectedMode) = chooseModeScreen.run(windowAfterDifficulty)

    (windowAfterMode, GameSetup(selectedMode = selectedMode, selectedDifficulty = selectedDifficulty))

  def buildAutomaticPlayerBuilder(mode: String): TeamBuilder = mode match
    case "Random" => RandomTeamBuilder()
    case "Affine" => AffineTeamBuilder()
    case _ => RandomTeamBuilder()