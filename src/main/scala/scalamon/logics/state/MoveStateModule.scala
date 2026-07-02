package scalamon.logics.state

import scalamon.domain.moves.Accuracy.Accuracy

trait MoveStateModule extends StateComponent:
  type MoveState
  type Move
  type PP
  override protected type State = MoveState

  def moveInitialState(move: Move): MoveState

  def currentPp(f: PP => PP): Op
  def decreasePpBy(value: PP): Op
  def increasePpBy(value: PP): Op

  extension (moveState: MoveState)
    def maxPp: PP
    def move: Move

object MoveStateModuleImpl extends MoveStateModule:
  import scalamon.domain.moves
  import moves.{DamageMove, StatusMove, Accuracy}
  

  case class Ms(move: Move, currentPp: PP)
  override type MoveState = Ms
  override type Move = moves.Move
  override type PP = Int

  override def moveInitialState(move: Move): MoveState = Ms(move, move.pp.asInt)

  def currentPp(f: PP => PP): Op = ms => ms.copy(currentPp = f(ms.currentPp).clamped(0, ms.maxPp))
  def decreasePpBy(value: PP): Op = currentPp(_ - value)
  def increasePpBy(value: PP): Op = currentPp(_ + value)
  def accuracyPercent(f: Int => Int): Op = ms => ms.move match
    case dm: DamageMove => ms.copy(move = dm.copy(accuracy = Accuracy.accuracyFromPercent(f(dm.accuracy.asInt))))
    case sm: StatusMove => ms.copy(move = sm.copy(accuracy = Accuracy.accuracyFromPercent(f(sm.accuracy.asInt))))

  extension (moveState: MoveState)
    def maxPp: PP = moveState.move.pp.asInt
    def move: Move = moveState.move


  extension (pp: PP)
    private def clamped(min: Int, max: Int): PP = pp.max(min).min(max)