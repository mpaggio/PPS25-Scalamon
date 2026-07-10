package scalamon.gui

/**
 * A trait representing the state of a battle window in a GUI application.
 * It defines methods for creating and manipulating the window, including setting its size,
 * adding buttons and labels, updating their text, showing the window, clearing its contents,
 * and retrieving the next event from the event queue.
 */
trait BattleWindowState:
  type Window
  def initialWindow: Window
  def setSize(width: Int, height: Int): State[Window, Unit]
  def addButton(text: String, name: String): State[Window, Unit]
  def addCenterButton(text: String, name: String): State[Window, Unit]
  def addLabel(text: String, name: String): State[Window, Unit]
  def addTextArea(text: String, name: String): State[Window, Unit]
  def addCenterLabel(text: String, name: String): State[Window, Unit]
  def updateLabel(text: String, name: String): State[Window, Unit]
  def updateButtonText(text: String, name: String): State[Window, Unit]
  def updateTextArea(text: String, name: String): State[Window, Unit]
  def show(): State[Window, Unit]
  def clear(): State[Window, Unit]
  def useMenuCenter(): State[Window, Unit]
  def useGridCenter(): State[Window, Unit]
  def nextEvent(): State[Window, String]

/**
 * An implementation of the BattleWindowState trait using a Swing-based GUI.
 * It provides concrete implementations for creating and manipulating the window,
 * including setting its size, adding buttons and labels, updating their text,
 * showing the window, clearing its contents, and retrieving the next event from the event queue.
 */
object BattleWindowStateImpl extends BattleWindowState:
  import SwingFacade.*
  type Window = Frame

  def initialWindow: Window = createFrame()

  def setSize(width: Int, height: Int): State[Window, Unit] =
    State(w => (w.setSize(width, height), ()))
  def addButton(text: String, name: String): State[Window, Unit] =
    State(w => (w.addButton(text, name), ()))
  def addCenterButton(text: String, name: String): State[Window, Unit] =
    State(w => (w.addCenterButton(text, name), ()))
  def addLabel(text: String, name: String): State[Window, Unit] =
    State(w => (w.addLabel(text, name), ()))
  def addTextArea(text: String, name: String): State[Window, Unit] =
    State(w => (w.addTextArea(text, name), ()))
  def addCenterLabel(text: String, name: String): State[Window, Unit] =
    State(w => (w.addCenterLabel(text, name), ()))
  def updateLabel(text: String, name: String): State[Window, Unit] =
    State(w => (w.updateLabel(text, name), ()))
  def updateButtonText(text: String, name: String): State[Window, Unit] =
    State(w => (w.updateButtonText(text, name), ()))
  def updateTextArea(text: String, name: String): State[Window, Unit] =
    State(w => (w.updateTextArea(text, name), ()))
  def show(): State[Window, Unit] =
    State(w => (w.show(), ()))
  def clear(): State[Window, Unit] =
    State(w => (w.clear(), ()))
  def useMenuCenter(): State[Window, Unit] =
    State(w => (w.useMenuCenter(), ()))
  def useGridCenter(): State[Window, Unit] =
    State(w => (w.useGridCenter(), ()))
  def nextEvent(): State[Window, String] =
    State(w => (w, w.nextEvent()))