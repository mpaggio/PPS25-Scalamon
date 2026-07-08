package scalamon.gui

import scala.swing.*
import scala.swing.event.ButtonClicked
import java.util.concurrent.LinkedBlockingQueue

/**
 * A facade for creating and managing a simple Swing GUI.
 */
object SwingFacade:
  /**
   * A trait representing a simple GUI frame with buttons and labels.
   * It provides methods to set the size of the frame, add buttons and labels,
   * update their text, show the frame, clear its contents, and retrieve the next event from the event queue.
   */
  trait Frame:
    def setSize(width: Int, height: Int): Frame
    def addButton(text: String, name: String): Frame
    def addLabel(text: String, name: String): Frame
    def updateLabel(text: String, name: String): Frame
    def updateButtonText(text: String, name: String): Frame
    def show(): Frame
    def clear(): Frame
    def nextEvent(): String

  def createFrame(): Frame = new FrameImpl()

  /**
   * An implementation of the Frame trait using Scala Swing.
   * It maintains a queue of events, a map of labels, and a map of buttons.
   * It provides methods to manipulate the GUI components and handle events.
   */
  private class FrameImpl extends Frame:
    private val eventQueue = new LinkedBlockingQueue[String]()
    private var labels = Map.empty[String, Label]
    private val buttons = scala.collection.mutable.Map[String, Button]()
    private val panel = new BoxPanel(Orientation.Vertical)
    private var lastEvent: Option[String] = None

    private val frame = new MainFrame:
      title = "Scalamon"
      contents = panel

    def setSize(width: Int, height: Int): Frame =
      frame.preferredSize = new Dimension(width, height)
      this

    private def notifyEvent(name: String): Unit =
      lastEvent = Some(name)
      eventQueue.put(name)

    def addButton(text: String, name: String): Frame =
      val button = new Button(text)
      button.name = name
      button.reactions += {
        case event.ButtonClicked(_) => notifyEvent(name)
      }
      buttons(name) = button
      panel.contents += button
      this

    def updateButtonText(text: String, name: String): Frame =
      buttons.get(name).foreach(_.text = text)
      panel.revalidate()
      panel.repaint()
      this

    def addLabel(text: String, labelName: String): Frame =
      val lbl = new Label(text)
      labels += (labelName -> lbl)
      panel.contents += lbl
      this

    def updateLabel(text: String, name: String): Frame =
      labels.get(name).foreach(_.text = text)
      this

    def show(): Frame =
      frame.pack()
      frame.visible = true
      this

    def clear(): Frame =
      panel.contents.clear()
      labels = Map()
      panel.revalidate()
      panel.repaint()
      this

    def nextEvent(): String = eventQueue.take()