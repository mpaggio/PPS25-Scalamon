package scalamon.gui

import scalamon.domain.moves.{DamageMove, Move, StatusMove}
import scalamon.domain.moves.MoveDatabase.{allMoves, findByName}
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.pokedex.MyPokedex.allPokemons
import scalamon.logics.teambuilder.ManualTeamBuilder.ManualTeamBuilder
import scalamon.logics.teambuilder.TeamBuilder.TeamBuilder
import BattleWindowStateImpl.*

import scala.annotation.tailrec

object ManualTeamBuildingGUI:

  private type ManualMoveSelection = Map[String, List[Move]]

  private def pokemonButtonName(pokemon: Pokemon): String =
    s"Pick_${pokemon.name}"

  private def pokemonTooltipText(pokemon: Pokemon): String =
    s"""<html>
       |<b>${pokemon.name}</b><br/>
       |Type: ${pokemon.pokemonType}<br/>
       |Stats: ${pokemon.baseStats}<br/>
       |Ability: ${pokemon.abilitySlot}
       |</html>""".stripMargin

  private def moveTooltipText(move: Move): String = move match
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
         |PP: ${status.pp}<br/>
         |</html>""".stripMargin

  private def moveButtonName(move: Move): String =
    s"MovePick_${move.name}"

  private def selectedTeamText(selected: List[Pokemon]): String =
    if selected.isEmpty then "No Pokemon Selected!"
    else s"Pokémon selected (${selected.size}/6): " + selected.map(_.name).mkString(", ")

  private def selectedMovesText(selected: List[Move]): String =
    if selected.isEmpty then "No moves selected!"
    else s"Moves selected (${selected.size}/4): " + selected.map(_.name).mkString(", ")

  private def manualTeamSelectionScreen: State[Window, Unit] = for
    _ <- clear()
    _ <- useGridCenter()
    _ <- setSize(900, 700)
    _ <- addLabel("Select exactly 6 Pokémon for your team:", "ManualSetupTitle")
    _ <- addLabel("Click on the Pokemon to add it to your team", "ManualSubTitle")
    _ <- addLabel("No Pokemon selected!", "SelectedTeamLabel")
    _ <- addLabel("Select 6 Pokémon, then press Confirm.", "ManualInfoLabel")
    _ <- allPokemons.foldLeft(State.unit[Window, Unit](())) { (acc, pokemon) =>
      for
        _ <- acc
        _ <- addButton(pokemon.name, pokemonButtonName(pokemon))
        _ <- setButtonTooltip(pokemonButtonName(pokemon), pokemonTooltipText(pokemon))
      yield ()
    }
    _ <- addButton("Cancel last choice", "ManualCancelLast")
    _ <- addButton("Reset the team", "ManualReset")
    _ <- addButton("Confirm", "ManualConfirm")
    _ <- show()
  yield ()

  private def chooseManualTeamScreen: State[Window, List[Pokemon]] =
    State { w =>
      val (_, _) = manualTeamSelectionScreen.run(w)

      @tailrec
      def loop(selected: List[Pokemon]): List[Pokemon] =
        w.updateLabel(selectedTeamText(selected), "SelectedTeamLabel")
        val event = w.nextEvent()

        event match
          case "ManualConfirm" =>
            if selected.size == 6 then selected
            else
              w.updateLabel(s"You have to select exactly 6 Pokemon. Now you have ${selected.size}.", "ManualInfoLabel")
              loop(selected)

          case buttonName if buttonName.startsWith("Pick_") =>
            val pokemonName = buttonName.stripPrefix("Pick_")
            val chosen = allPokemons.find(_.name == pokemonName)

            chosen match
              case Some(pokemon) if selected.exists(_.name == pokemon.name) =>
                loop(selected.filterNot(_.name == pokemon.name))
              case Some(pokemon) if selected.size < 6 =>
                loop(selected :+ pokemon)
              case Some(pokemon) =>
                w.updateLabel(s"You already selected 6 Pokémon. ${pokemon.name} cannot be added to the team.", "ManualInfoLabel")
                loop(selected)
              case None =>
                w.updateLabel("Pokémon not found.", "ManualInfoLabel")
                loop(selected)

          case "ManualCancelLast" =>
            loop(if selected.nonEmpty then selected.init else selected)

          case "ManualReset" =>
            w.updateLabel("Team Reset.", "ManualInfoLabel")
            loop(Nil)

          case _ =>
            loop(selected)

      val selectedTeam = loop(Nil)
      (w, selectedTeam)
    }

  private def manualMoveSelectionScreen(pokemon: Pokemon): State[Window, Unit] = for
    _ <- clear()
    _ <- useGridCenter()
    _ <- setSize(900, 700)
    _ <- addLabel(s"Select exactly 4 moves for ${pokemon.name}:", "MoveSetupTitle")
    _ <- addLabel("Click on a move to add/remove it", "MoveSubTitle")
    _ <- addLabel("No moves selected!", "SelectedMovesLabel")
    _ <- addLabel("Select 4 moves, then press Confirm.", "MoveInfoLabel")
    _ <- allMoves.toList.foldLeft(State.unit[Window, Unit](())) { (acc, move) =>
      for
        _ <- acc
        _ <- addButton(move.name, moveButtonName(move))
        _ <- setButtonTooltip(moveButtonName(move), moveTooltipText(move))
      yield ()
    }
    _ <- addButton("Cancel last choice", "CancelLastMoveChoice")
    _ <- addButton("Reset moves", "ResetManualMoves")
    _ <- addButton("Confirm", "ConfirmManualMoves")
    _ <- show()
  yield ()

  private def chooseMovesForPokemonScreen(pokemon: Pokemon): State[Window, List[Move]] =
    State { w =>
      val (_, _) = manualMoveSelectionScreen(pokemon).run(w)

      @tailrec
      def loop(selected: List[Move]): List[Move] =
        w.updateLabel(selectedMovesText(selected), "SelectedMovesLabel")
        val event = w.nextEvent()

        event match
          case "ConfirmManualMoves" =>
            if selected.size == 4 then selected
            else
              w.updateLabel(s"You have to select exactly 4 moves. Now you have ${selected.size}.", "MoveInfoLabel")
              loop(selected)

          case buttonName if buttonName.startsWith("MovePick_") =>
            val moveName = buttonName.stripPrefix("MovePick_")
            val chosen = allMoves.findByName(moveName)

            chosen match
              case Some(move) if selected.exists(_.name == move.name) =>
                loop(selected.filterNot(_.name == move.name))
              case Some(move) if selected.size < 4 =>
                loop(selected :+ move)
              case Some(move) =>
                w.updateLabel(s"You already selected 4 moves. ${move.name} cannot be added.", "MoveInfoLabel")
                loop(selected)
              case None =>
                w.updateLabel("Move not found.", "MoveInfoLabel")
                loop(selected)

          case "CancelLastMoveChoice" =>
            loop(if selected.nonEmpty then selected.init else selected)

          case "ResetManualMoves" =>
            w.updateLabel("Moves reset.", "MoveInfoLabel")
            loop(Nil)

          case _ =>
            loop(selected)

      val selectedMoves = loop(Nil)
      (w, selectedMoves)
    }

  private def chooseManualMovesScreen(team: List[Pokemon]): State[Window, ManualMoveSelection] =
    State { w =>
      team.foldLeft((w, Map.empty[String, List[Move]])) { case ((currentWindow, acc), pokemon) =>
        val (nextWindow, moves) = chooseMovesForPokemonScreen(pokemon).run(currentWindow)
        (nextWindow, acc + (pokemon.name -> moves))
      }
    }

  private def buildManualBuilderFromSelection(
                                               selectedTeam: List[Pokemon],
                                               selectedMoves: ManualMoveSelection
                                             ): TeamBuilder =
    ManualTeamBuilder(
      pokemonSelector = _ => selectedTeam,
      moveSelector = (pokemon, _) => selectedMoves.getOrElse(pokemon.name, Nil)
    )

  def chooseManualBuilder(window: Window): (Window, TeamBuilder) =
    val (w1, selectedTeam) = chooseManualTeamScreen.run(window)
    val (w2, selectedMoves) = chooseManualMovesScreen(selectedTeam).run(w1)
    (w2, buildManualBuilderFromSelection(selectedTeam, selectedMoves))