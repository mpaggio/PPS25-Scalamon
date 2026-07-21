package scalamon.view

import scalamon.controller.*
import scalamon.logics.teambuilder.TeamBuilder.*
import scalamon.util.StateMonad
import scalamon.util.StateMonad.*
import scalamon.view.SwingFacade.*
import scalamon.view.Widgets.*
import scalamon.view.HtmlTooltips.*
import Picker.io

import javax.swing.JOptionPane
import scala.annotation.tailrec

/**
 * Swing implementation of the GameView port.
 * All Swing-specific concerns live here: window sizes, HTML markup for
 * buttons and tooltips, dialogs, blocking event loops.
 */
object View extends ViewModel:
  opaque type V = Frame

  def initial: V = createFrame()

  private val MenuSize = (500, 320)
  private val BattleSize = (500, 600)

  /** Busy-wait for an event that satisfies the given predicate. */
  private def waitEvent(accept: String => Boolean): StateMonad[V, String] =
    StateMonad(w =>
      @tailrec def loop(): String =
        val event = w.nextEvent()
        if accept(event) then event else loop()
      (w, loop())
    )

  private def message(text: String): Unit = JOptionPane.showMessageDialog(null, text)

  private def announceDialog(text: String): Unit =
    JOptionPane.showMessageDialog(null, text, "Switch player!", JOptionPane.INFORMATION_MESSAGE)

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


  private def menuScreen(subtitle: String, options: List[String]): StateMonad[V, String] = for
    _ <- io(_.clear().useMenuCenter().setSize(MenuSize._1, MenuSize._2))
    _ <- io(_.addCenterLabel("SCALAMON", "MenuTitle"))
    _ <- io(_.addCenterLabel(subtitle, "MenuSubtitle"))
    _ <- StateMonad.traverse(options)(option => io(_.addCenterButton(option, option)))
    _ <- io(_.show())
    event <- waitEvent(options.contains)
  yield event

  private def moveButtonName(index: Int): String = s"$MovePrefix${index + 1}"

  private def moveButtonText(slot: MoveSlot): String =
    s"<html>${slot.name}<br>(PP = ${slot.currentPp}/${slot.maxPp})</html>"

  /** One text per move button, padding with "-" up to the configured slot count. */
  private def moveButtonTexts(moves: List[MoveSlot]): List[String] =
    (0 until GameConfig.MovesPerPokemon).toList
      .map(i => moves.lift(i).fold("-")(moveButtonText))

  def chooseDifficulty: StateMonad[V, Difficulty] =
    menuScreen("Select the difficulty:", Difficulty.values.toList.map(_.toString))
      .map(Difficulty.valueOf)

  def chooseMode: StateMonad[V, Mode] =
    menuScreen("Select the team building mode:", Mode.values.toList.map(_.toString))
      .map(Mode.valueOf)

  /**
   * Wraps an interactive flow into a deferred selector: the returned function
   * captures the window and runs its screens only when the TeamBuilder
   * invokes it.
   */
  private def deferred[F](make: V => F): StateMonad[V, F] = StateMonad(w => (w, make(w)))
  
  def chooseTeam(player: String): StateMonad[V, PokemonSelector] =
    deferred(w => (available, size) =>
      Picker.pickExactly(
        Picker(player, "Pokémon", size, available, _.name, pokemonTooltip),
        s"Select exactly $size Pokémon for your team:"
      ).run(w)._2)

  def chooseMoves(player: String): StateMonad[V, MoveSelector] =
    deferred(w => (pokemon, available, size) =>
      Picker.pickExactly(
        Picker(player, "moves", size, available, _.name, moveTooltip),
        s"Select exactly $size moves for ${pokemon.name}:"
      ).run(w)._2)

  def chooseItems(player: String): StateMonad[V, ItemSelector] =
    deferred(w => (available, size) =>
      Picker.pickExactly(
        Picker(player, "items", size, available.toList, _.name, itemTooltip),
        s"Select exactly $size items:"
      ).run(w)._2.toSet)

  def showBattleScreen(vm: BattleViewModel, setupLog: String): StateMonad[V, Unit] = for
    _ <- io(_.clear().setSize(BattleSize._1, BattleSize._2))
    _ <- io(_.addLabel(vm.status, BattleStatus))
    _ <- io(_.addLabel(vm.weather, WeatherStatus))
    _ <- io(_.addTextArea("", BattleLog))
    _ <- StateMonad.traverse(moveButtonTexts(vm.moves).zipWithIndex)((text, i) => io(_.addButton(text, moveButtonName(i))))
    _ <- io(_.addButton("Switch Pokemon", SwitchMenu))
    _ <- io(_.addButton("Use item", ItemMenu))
    _ <- io(_.show())
    _ <- io(_.updateTextArea(setupLog, BattleLog))
  yield ()

  def renderBattle(vm: BattleViewModel): StateMonad[V, Unit] =
    io(_.updateTextArea(vm.log, BattleLog)
        .updateLabel(vm.status, BattleStatus)
        .updateLabel(vm.weather, WeatherStatus))

  def askAction(prompt: ActionPrompt): StateMonad[V, PlayerIntent] =
    StateMonad(w =>
      moveButtonTexts(prompt.moves).zipWithIndex.foreach((text, i) => w.updateButtonText(text, moveButtonName(i)))

      @tailrec def loop(): PlayerIntent = w.nextEvent() match
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
    )

  def askForcedSwitch(msg: String, candidates: List[String]): StateMonad[V, String] =
    StateMonad(w =>
      val chosen = choiceDialog(msg, "MandatorySwitch", candidates, identity, JOptionPane.WARNING_MESSAGE)
        .getOrElse(candidates.head)
      (w, chosen)
    )

  def announce(text: String): StateMonad[V, Unit] =
    StateMonad(w =>
      announceDialog(text)
      (w, ())
    )

  def close: StateMonad[V, Unit] = io(_.close())
