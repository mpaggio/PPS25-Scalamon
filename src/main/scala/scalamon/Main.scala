package scalamon

import scalamon.app.GameLoop
import scalamon.view.SwingGameView

@main def runScalamonGUI(): Unit =
  GameLoop(SwingGameView).run()
