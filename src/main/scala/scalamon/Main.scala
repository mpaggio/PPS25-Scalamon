package scalamon

import scalamon.app.GameApp
import scalamon.view.SwingGameView

@main def runScalamonGUI(): Unit =
  GameApp(SwingGameView).run()
