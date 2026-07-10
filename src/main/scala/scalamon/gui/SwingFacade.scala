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
    def addCenterButton(text: String, name: String): Frame
    def addLabel(text: String, name: String): Frame
    def addTextArea(txt: String, name: String): Frame
    def addCenterLabel(text: String, name: String): Frame
    def updateLabel(text: String, name: String): Frame
    def updateButtonText(text: String, name: String): Frame
    def updateTextArea(text: String, name: String): Frame
    def setButtonTooltip(name: String, text: String): Frame
    def show(): Frame
    def clear(): Frame
    def useMenuCenter(): Frame
    def useGridCenter(): Frame
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
    private var textAreas = Map.empty[String, TextArea]

    private val topPanel = new BoxPanel(Orientation.Vertical)
    private val bottomPanel = new BoxPanel(Orientation.Horizontal)
    private val gridPanel = new GridPanel(0, 4)
    private val menuPanel = new BoxPanel(Orientation.Vertical)

    private var currentCenter: Component = menuPanel

    private val rootPanel = new BorderPanel:
      add(topPanel, BorderPanel.Position.North)
      add(menuPanel, BorderPanel.Position.Center)
      add(bottomPanel, BorderPanel.Position.South)

    private val frame = new MainFrame:
      title = "Scalamon"
      contents = rootPanel

    def setSize(width: Int, height: Int): Frame =
      frame.preferredSize = new Dimension(width, height)
      this

    private def notifyEvent(name: String): Unit =
      eventQueue.put(name)

    private def switchCenter(newCenter: Component): Unit =
      rootPanel.layout -= currentCenter
      currentCenter = newCenter
      rootPanel.layout(currentCenter) = BorderPanel.Position.Center
      rootPanel.revalidate()
      rootPanel.repaint()

    def useMenuCenter(): Frame =
      switchCenter(menuPanel)
      this

    def useGridCenter(): Frame =
      switchCenter(new ScrollPane(gridPanel))
      this

    def addButton(text: String, name: String): Frame =
      val button = new Button(text)
      button.name = name
      button.listenTo(button)
      button.reactions += {
        case ButtonClicked(_) => notifyEvent(name)
      }
      button.listenTo(button)
      buttons(name) = button
      if name.startsWith("Pick_") || name.startsWith("MovePick_") then
        button.preferredSize = new Dimension(180, 60)
        gridPanel.contents += button
      else
        bottomPanel.contents += button
      rootPanel.revalidate()
      rootPanel.repaint()
      this

    def addCenterButton(text: String, name: String): Frame =
      val button = new Button(text)
      button.name = name
      button.preferredSize = new Dimension(220, 45)
      button.maximumSize = new Dimension(220, 45)
      button.xLayoutAlignment = 0.5
      button.reactions += {
        case ButtonClicked(_) => notifyEvent(name)
      }
      button.listenTo(button)
      buttons(name) = button
      menuPanel.contents += Swing.VStrut(10)
      menuPanel.contents += button
      rootPanel.revalidate()
      rootPanel.repaint()
      this

    def updateButtonText(text: String, name: String): Frame =
      buttons.get(name).foreach(_.text = text)
      rootPanel.revalidate()
      rootPanel.repaint()
      this

    def setButtonTooltip(name: String, text: String): Frame =
      buttons.get(name).foreach(_.tooltip = text)
      this

    def addLabel(text: String, labelName: String): Frame =
      val lbl = new Label(text)
      labels += (labelName -> lbl)
      topPanel.contents += lbl
      rootPanel.revalidate()
      rootPanel.repaint()
      this

    def addCenterLabel(text: String, labelName: String): Frame =
      val lbl = new Label(text)
      lbl.xAlignment = Alignment.Center
      lbl.xLayoutAlignment = 0.5
      labels += (labelName -> lbl)
      menuPanel.contents += Swing.VStrut(10)
      menuPanel.contents += lbl
      rootPanel.revalidate()
      rootPanel.repaint()
      this

    def updateLabel(text: String, name: String): Frame =
      labels.get(name).foreach(_.text = text)
      panel.revalidate()
      panel.repaint()
      rootPanel.revalidate()
      rootPanel.repaint()
      this

    def show(): Frame =
      frame.pack()
      frame.visible = true
      this

    def clear(): Frame =
      topPanel.contents.clear()
      menuPanel.contents.clear()
      gridPanel.contents.clear()
      bottomPanel.contents.clear()
      labels = Map()
      panel.revalidate()
      panel.repaint()
      textAreas = Map.empty
      buttons.clear()
      switchCenter(menuPanel)
      this

    def nextEvent(): String = eventQueue.take()

    override def addTextArea(txt: String, name: String): Frame =
      val textArea = new TextArea{
        editable = false
        rows = 12
        lineWrap = true
        wordWrap = true
        text = txt
      }
      textAreas += (name -> textArea)
      panel.contents += new ScrollPane(textArea)
      switchCenter(panel)
      rootPanel.revalidate()
      rootPanel.repaint()
      this

    override def updateTextArea(text: String, name: String): Frame =
      textAreas.get(name).foreach: ta =>
        ta.append(text + "\n")
        ta.caret.position = ta.text.length
      panel.revalidate()
      panel.repaint()
      this