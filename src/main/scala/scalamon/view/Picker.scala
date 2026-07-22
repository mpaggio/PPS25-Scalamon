package scalamon.view

import scalamon.util.StateMonad
import scalamon.view.SwingFacade.Frame
import scalamon.view.Widgets.*
import scala.annotation.tailrec

/** Configuration of a selection screen: label texts and how to render each item. */
private[view] case class Picker[A](
                            player: String,
                            itemLabel: String,
                            size: Int,
                            items: List[A],
                            name: A => String,
                            tooltip: A => String
                          )


private[view] object Picker:

  private val PickerSize = (900, 700)
  

  /** Lifts a fluent Frame operation into the State monad. */
  def io(f: Frame => Frame): StateMonad[Frame, Unit] = StateMonad(w => (f(w), ()))

  /** Toggle-based selection loop: confirm succeeds only with exactly `p.size` items. */
  def pickExactly[A](p: Picker[A], title: String): StateMonad[Frame, List[A]] = for
    _ <- pickerScreen(p, title)
    selected <- StateMonad((w: Frame) =>
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
    )
  yield selected


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
