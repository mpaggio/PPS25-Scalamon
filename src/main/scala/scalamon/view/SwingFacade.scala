package scalamon.view

import java.util.concurrent.LinkedBlockingQueue
import scala.swing.*
import scala.swing.event.ButtonClicked

/**
 * A facade for creating and managing a simple Swing GUI.
 */
object SwingFacade:

  /**
   * A simple GUI frame with buttons, labels and text areas.
   * Every mutating method returns the frame itself, so that it can be
   * lifted into the State monad by the Swing view.
   */
  trait Frame:
    def setSize(width: Int, height: Int): Frame
    def addButton(text: String, name: String): Frame
    def addCenterButton(text: String, name: String): Frame
    def addLabel(text: String, name: String): Frame
    def addTextArea(text: String, name: String): Frame
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
    def close(): Frame

  def createFrame(): Frame = new FrameImpl()

  /**
   * Swing implementation of Frame. It maintains an event queue fed by the
   * button listeners and consumed (blocking) by nextEvent().
   * Component placement is decided from the widget name, using the
   * conventions declared in Widgets.
   */
  private class FrameImpl extends Frame:
    import Widgets.*

    private val eventQueue = new LinkedBlockingQueue[String]()
    private var labels = Map.empty[String, Label]
    private var textAreas = Map.empty[String, TextArea]
    private val buttons = scala.collection.mutable.Map[String, Button]()

    private val logPanel = new BoxPanel(Orientation.Vertical)          // TEXT AREAS & LOGGER
    private val topPanel = new BoxPanel(Orientation.Vertical)          // TOP PART
    private val statusRowPanel = new BoxPanel(Orientation.Horizontal)  // BATTLE & WEATHER STATUS ROW
    private val bottomPanel = new BoxPanel(Orientation.Vertical)       // BOTTOM PART WITH BUTTONS
    private val moveRowPanel = new BoxPanel(Orientation.Horizontal)    // MOVE BUTTONS
    private val actionRowPanel = new BoxPanel(Orientation.Horizontal)  // SWITCH & ITEM BUTTONS
    private val pickActionRowPanel = new BoxPanel(Orientation.Horizontal) // CONFIRM/CANCEL/RESET
    private val gridPanel = new GridPanel(0, 4)                        // SELECTION GRIDS
    private val menuPanel = new BoxPanel(Orientation.Vertical)         // MENU & SETUP

    topPanel.contents += statusRowPanel
    bottomPanel.contents ++= Seq(moveRowPanel, actionRowPanel, pickActionRowPanel)

    private var currentCenter: Component = menuPanel

    private val rootPanel = new BorderPanel:
      add(topPanel, BorderPanel.Position.North)
      add(menuPanel, BorderPanel.Position.Center)
      add(bottomPanel, BorderPanel.Position.South)

    private val frame = new MainFrame:
      title = "Scalamon"
      contents = rootPanel

    // ---- internal helpers ----

    private def refresh(): Unit =
      rootPanel.revalidate()
      rootPanel.repaint()

    private def notifyEvent(name: String): Unit =
      eventQueue.put(name)

    private def switchCenter(newCenter: Component): Unit =
      rootPanel.layout -= currentCenter
      currentCenter = newCenter
      rootPanel.layout(currentCenter) = BorderPanel.Position.Center
      refresh()

    private def sized(button: Button, width: Int, height: Int): Button =
      button.preferredSize = new Dimension(width, height)
      button.maximumSize = new Dimension(width, height)
      button

    private def newButton(text: String, name: String): Button =
      val button = new Button(text)
      button.name = name
      button.listenTo(button)
      button.reactions += { case ButtonClicked(_) => notifyEvent(name) }
      buttons(name) = button
      button

    // ---- Frame implementation ----

    def setSize(width: Int, height: Int): Frame =
      frame.preferredSize = new Dimension(width, height)
      this

    def useMenuCenter(): Frame =
      switchCenter(menuPanel)
      this

    def useGridCenter(): Frame =
      switchCenter(new ScrollPane(gridPanel))
      this

    def addButton(text: String, name: String): Frame =
      val button = newButton(text, name)
      name match
        case n if n.startsWith(PickPrefix) =>
          button.preferredSize = new Dimension(180, 60)
          gridPanel.contents += button
        case n if PickActions(n) =>
          pickActionRowPanel.contents += sized(button, 200, 45)
        case n if n.startsWith(MovePrefix) =>
          moveRowPanel.contents += sized(button, 120, 45)
        case SwitchMenu | ItemMenu =>
          actionRowPanel.contents += sized(button, 240, 45)
        case _ =>
          bottomPanel.contents += button
      refresh()
      this

    def addCenterButton(text: String, name: String): Frame =
      val button = sized(newButton(text, name), 220, 45)
      button.xLayoutAlignment = 0.5
      menuPanel.contents += Swing.VStrut(10)
      menuPanel.contents += button
      refresh()
      this

    def updateButtonText(text: String, name: String): Frame =
      buttons.get(name).foreach(_.text = text)
      refresh()
      this

    def setButtonTooltip(name: String, text: String): Frame =
      buttons.get(name).foreach(_.tooltip = text)
      this

    def addLabel(text: String, name: String): Frame =
      val label = new Label(text)
      labels += (name -> label)
      if name == BattleStatus || name == WeatherStatus then
        if name == WeatherStatus then
          statusRowPanel.contents += Swing.HGlue // puts the weather to the right side
        statusRowPanel.contents += label
      else
        topPanel.contents += label
      refresh()
      this

    def addCenterLabel(text: String, name: String): Frame =
      val label = new Label(text)
      label.xAlignment = Alignment.Center
      label.xLayoutAlignment = 0.5
      labels += (name -> label)
      menuPanel.contents += Swing.VStrut(10)
      menuPanel.contents += label
      refresh()
      this

    def updateLabel(text: String, name: String): Frame =
      labels.get(name).foreach(_.text = text)
      refresh()
      this

    def addTextArea(txt: String, name: String): Frame =
      val textArea = new TextArea {
        editable = false
        rows = 12
        lineWrap = true
        wordWrap = true
        text = txt
      }
      textAreas += (name -> textArea)
      logPanel.contents += new ScrollPane(textArea)
      switchCenter(logPanel)
      refresh()
      this

    def updateTextArea(text: String, name: String): Frame =
      textAreas.get(name).foreach { ta =>
        ta.append(text + "\n")
        ta.caret.position = ta.text.length
      }
      logPanel.revalidate()
      logPanel.repaint()
      this

    def show(): Frame =
      frame.pack()
      frame.visible = true
      this

    def clear(): Frame =
      topPanel.contents.clear()
      statusRowPanel.contents.clear()
      topPanel.contents += statusRowPanel

      moveRowPanel.contents.clear()
      actionRowPanel.contents.clear()
      pickActionRowPanel.contents.clear()
      bottomPanel.contents.clear()
      bottomPanel.contents ++= Seq(moveRowPanel, actionRowPanel, pickActionRowPanel)

      menuPanel.contents.clear()
      gridPanel.contents.clear()
      logPanel.contents.clear()

      labels = Map.empty
      textAreas = Map.empty
      buttons.clear()
      switchCenter(menuPanel)
      this

    def nextEvent(): String = eventQueue.take()

    def close(): Frame =
      frame.dispose()
      this
