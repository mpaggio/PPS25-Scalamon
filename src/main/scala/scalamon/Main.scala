package scalamon

import scalamon.controller.GameLoop
import scalamon.view.View

@main def runScalamonGUI(): Unit =
  GameLoop(View).run()
