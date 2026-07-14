package scalamon.view

/**
 * Names of the widgets shared between the Swing view and the facade,
 * so that no string literal is duplicated across the two files.
 * The facade uses them to decide where each component is placed.
 */
private[view] object Widgets:
  // battle screen
  val BattleStatus = "BattleStatus"
  val WeatherStatus = "WeatherStatus"
  val BattleLog = "BattleLog"
  val MovePrefix = "Move"
  val SwitchMenu = "SwitchMenu"
  val ItemMenu = "ItemMenu"

  // generic "pick exactly N items" screen
  val PickPrefix = "Pick_"
  val PickConfirm = "PickConfirm"
  val PickCancelLast = "PickCancelLast"
  val PickReset = "PickReset"
  val PickActions: Set[String] = Set(PickConfirm, PickCancelLast, PickReset)
