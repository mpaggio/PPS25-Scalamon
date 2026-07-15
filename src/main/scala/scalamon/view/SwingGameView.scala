package scalamon.view

import scalamon.app.*
import scalamon.domain.actions.Items.Item
import scalamon.domain.moves.{DamageMove, Move, StatusMove}
import scalamon.domain.pokemon.Pokemon
import scalamon.logics.teambuilder.TeamBuilder.*
import scalamon.util.StateMonad
import scalamon.util.StateMonad.*
import scalamon.view.SwingFacade.*
import scalamon.view.Widgets.*

import javax.swing.JOptionPane
import scala.annotation.tailrec

/**
 * Swing implementation of the GameView port.
 *
 * All Swing-specific concerns live here: window sizes, HTML markup for
 * buttons and tooltips, dialogs, blocking event loops. A terminal view
 * could replace this object without touching the application layer.
 */
object SwingGameView extends GameView:
  type V = Frame

  def initial: V = createFrame()

  private val MenuSize = (500, 320)
  private val PickerSize = (900, 700)
  private val BattleSize = (500, 600)

  /** Lifts a fluent Frame operation into the State monad. */
  private def io(f: Frame => Frame): StateMonad[Frame, Unit] =
    StateMonad(w => (f(w), ()))

  /** Blocks until an event accepted by the predicate arrives. */
  private def waitEvent(accept: String => Boolean): StateMonad[Frame, String] =
    StateMonad { w =>
      @tailrec def loop(): String =
        val event = w.nextEvent()
        if accept(event) then event else loop()
      (w, loop())
    }

  // ---------- dialogs ----------

  private def message(text: String): Unit =
    JOptionPane.showMessageDialog(null, text)

  private def announceDialog(text: String): Unit =
    JOptionPane.showMessageDialog(null, text, "Switch player!", JOptionPane.INFORMATION_MESSAGE)

  /** Shows a selection dialog; returns None when there are no options or the user cancels. */
  private def choiceDialog[A](message: String, title: String, options: List[A], render: A => String,
                              messageType: Int = JOptionPane.QUESTION_MESSAGE): Option[A] =
    options match
      case Nil => None
      case _ =>
        val labels = options.map(render)
        val selection = JOptionPane.showInputDialog(
          null, message, title, messageType, null,
          labels.toArray.asInstanceOf[Array[Object]], labels.head
        )
        Option(selection).map(_.toString).flatMap(l => options.find(render(_) == l))

  // ---------- setup menus ----------

  private def menuScreen(subtitle: String, options: List[String]): StateMonad[Frame, String] = for
    _ <- io(_.clear().useMenuCenter().setSize(MenuSize._1, MenuSize._2))
    _ <- io(_.addCenterLabel("SCALAMON", "MenuTitle"))
    _ <- io(_.addCenterLabel(subtitle, "MenuSubtitle"))
    _ <- StateMonad.traverse(options)(option => io(_.addCenterButton(option, option)))
    _ <- io(_.show())
    event <- waitEvent(options.contains)
  yield event

  def chooseDifficulty: StateMonad[Frame, Difficulty] =
    menuScreen("Select the difficulty:", Difficulty.values.toList.map(_.toString))
      .map(Difficulty.valueOf)

  def chooseMode: StateMonad[Frame, Mode] =
    menuScreen("Select the team building mode:", Mode.values.toList.map(_.toString))
      .map(Mode.valueOf)

  // ---------- team building ----------

  /** Configuration of a selection screen: label texts and how to render each item. */
  private final case class Picker[A](
    player: String,
    itemLabel: String,
    size: Int,
    items: List[A],
    name: A => String,
    tooltip: A => String
  )

  private def pickerScreen[A](p: Picker[A], title: String): StateMonad[Frame, Unit] = for
    _ <- io(_.clear().useGridCenter().setSize(PickerSize._1, PickerSize._2))
    _ <- io(_.addLabel(s"${p.player}: $title", "PickTitle"))
    _ <- io(_.addLabel(s"${p.player}: Click on an option to add/remove it", "PickSubtitle"))
    _ <- io(_.addLabel(s"${p.player}: No ${p.itemLabel} selected!", "PickSelectedLabel"))
    _ <- io(_.addLabel(s"${p.player}: Select ${p.size} ${p.itemLabel}, then press Confirm.", "PickInfoLabel"))
    _ <- StateMonad.traverse(p.items)(item =>
      io(_.addButton(p.name(item), PickPrefix + p.name(item))
          .setButtonTooltip(PickPrefix + p.name(item), p.tooltip(item))))
    _ <- io(_.addButton("Cancel last choice", PickCancelLast))
    _ <- io(_.addButton("Reset", PickReset))
    _ <- io(_.addButton("Confirm", PickConfirm))
    _ <- io(_.show())
  yield ()

  private def selectionText[A](p: Picker[A], selected: List[A]): String =
    if selected.isEmpty then s"${p.player}: No ${p.itemLabel} selected!"
    else s"${p.player}: ${p.itemLabel} selected (${selected.size}/${p.size}): " +
      selected.map(p.name).mkString(", ")

  /** Toggle-based selection loop: confirm succeeds only with exactly `p.size` items. */
  private def pickExactly[A](p: Picker[A], title: String): StateMonad[Frame, List[A]] = for
    _ <- pickerScreen(p, title)
    selected <- StateMonad { (w: Frame) =>
      @tailrec def loop(selected: List[A]): List[A] =
        w.updateLabel(selectionText(p, selected), "PickSelectedLabel")
        w.nextEvent() match
          case PickConfirm if selected.size == p.size => selected
          case PickConfirm =>
            w.updateLabel(s"${p.player}: You have to select exactly ${p.size} ${p.itemLabel}. " +
              s"Now you have ${selected.size}.", "PickInfoLabel")
            loop(selected)
          case event if event.startsWith(PickPrefix) =>
            val itemName = event.stripPrefix(PickPrefix)
            p.items.find(item => p.name(item) == itemName) match
              case Some(_) if selected.exists(s => p.name(s) == itemName) =>
                loop(selected.filterNot(s => p.name(s) == itemName))
              case Some(item) if selected.size < p.size =>
                loop(selected :+ item)
              case Some(item) =>
                w.updateLabel(s"${p.player}: You already selected ${p.size} ${p.itemLabel}. " +
                  s"${p.name(item)} cannot be added.", "PickInfoLabel")
                loop(selected)
              case None =>
                loop(selected)
          case PickCancelLast =>
            loop(if selected.nonEmpty then selected.init else selected)
          case PickReset =>
            w.updateLabel(s"${p.player}: Selection reset.", "PickInfoLabel")
            loop(Nil)
          case _ =>
            loop(selected)
      (w, loop(Nil))
    }
  yield selected

  private def pokemonTooltip(pokemon: Pokemon): String =
    s"""<html>
       |<b>${pokemon.name}</b><br/>
       |Type: ${pokemon.pokemonType}<br/>
       |Stats: ${pokemon.baseStats}<br/>
       |Ability: ${pokemon.abilitySlot}
       |</html>""".stripMargin

  private def moveTooltip(move: Move): String = move match
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

  private def itemTooltip(item: Item): String =
    s"""<html>
       |<b>${item.name}</b><br/>
       |${item.description}
       |</html>""".stripMargin

  /**
   * Wraps an interactive flow into a deferred selector: the returned function
   * captures the window and runs its screens only when the TeamBuilder
   * invokes it. This steps outside the State threading on purpose — it is
   * sound because the Swing Frame mutates in place.
   */
  private def deferred[F](make: Frame => F): StateMonad[Frame, F] =
    StateMonad(w => (w, make(w)))
  
  def chooseTeam(player: String): StateMonad[Frame, PokemonSelector] =
    deferred(w => (available, size) =>
      pickExactly(
        Picker(player, "Pokémon", size, available, _.name, pokemonTooltip),
        s"Select exactly $size Pokémon for your team:"
      ).run(w)._2)

  def chooseMoves(player: String): StateMonad[Frame, MoveSelector] =
    deferred(w => (pokemon, available, size) =>
      pickExactly(
        Picker(player, "moves", size, available, _.name, moveTooltip),
        s"Select exactly $size moves for ${pokemon.name}:"
      ).run(w)._2)

  def chooseItems(player: String): StateMonad[Frame, ItemSelector] =
    deferred(w => (available, size) =>
      pickExactly(
        Picker(player, "items", size, available.toList, _.name, itemTooltip),
        s"Select exactly $size items:"
      ).run(w)._2.toSet)

  // ---------- battle screen ----------
  
  private def moveButtonName(index: Int): String = s"$MovePrefix${index + 1}"

  private def moveButtonText(slot: MoveSlot): String =
    s"<html>${slot.name}<br>(PP = ${slot.currentPp}/${slot.maxPp})</html>"

  /** One text per move button, padding with "-" up to the configured slot count. */
  private def moveButtonTexts(moves: List[MoveSlot]): List[String] =
    (0 until GameConfig.MovesPerPokemon).toList
      .map(i => moves.lift(i).fold("-")(moveButtonText))

  def showBattleScreen(vm: BattleViewModel, setupLog: String): StateMonad[Frame, Unit] = for
    _ <- io(_.clear().setSize(BattleSize._1, BattleSize._2))
    _ <- io(_.addLabel(vm.status, BattleStatus))
    _ <- io(_.addLabel(vm.weather, WeatherStatus))
    _ <- io(_.addTextArea("", BattleLog))
    _ <- StateMonad.traverse(moveButtonTexts(vm.moves).zipWithIndex)((text, i) =>
      io(_.addButton(text, moveButtonName(i))))
    _ <- io(_.addButton("Switch Pokemon", SwitchMenu))
    _ <- io(_.addButton("Use item", ItemMenu))
    _ <- io(_.show())
    _ <- io(_.updateTextArea(setupLog, BattleLog))
  yield ()

  def renderBattle(vm: BattleViewModel): StateMonad[Frame, Unit] =
    io(_.updateTextArea(vm.log, BattleLog)
        .updateLabel(vm.status, BattleStatus)
        .updateLabel(vm.weather, WeatherStatus))

  def askAction(prompt: ActionPrompt): StateMonad[Frame, PlayerIntent] =
    StateMonad { w =>
      moveButtonTexts(prompt.moves).zipWithIndex
        .foreach((text, i) => w.updateButtonText(text, moveButtonName(i)))

      @tailrec def loop(): PlayerIntent =
        w.nextEvent() match
          case SwitchMenu =>
            if prompt.switchable.isEmpty then
              message("No available Pokemon")
              loop()
            else
              choiceDialog("Select a Pokemon to switch to:", "Switch", prompt.switchable, identity) match
                case Some(name) => PlayerIntent.Switch(name)
                case None       => loop()
          case ItemMenu =>
            if prompt.items.isEmpty then
              message("No available items")
              loop()
            else
              choiceDialog("Select an item to use:", "Items", prompt.items,
                item => s"${item.name} - ${item.description}") match
                case Some(item) => PlayerIntent.Item(item.name)
                case None       => loop()
          case event if event.startsWith(MovePrefix) =>
            val index = event.stripPrefix(MovePrefix).toInt - 1
            prompt.moves.lift(index) match
              case Some(slot) => PlayerIntent.Attack(slot.name)
              case None       => loop()
          case _ =>
            loop()

      (w, loop())
    }

  def askForcedSwitch(msg: String, candidates: List[String]): StateMonad[Frame, String] =
    StateMonad { w =>
      val chosen = choiceDialog(msg, "MandatorySwitch", candidates, identity, JOptionPane.WARNING_MESSAGE)
        .getOrElse(candidates.head)
      (w, chosen)
    }

  def announce(text: String): StateMonad[Frame, Unit] =
    StateMonad { w =>
      announceDialog(text)
      (w, ())
    }

  def close: StateMonad[Frame, Unit] = io(_.close())
