package scalamon.logics.state

trait MoveStateModule:
  type MoveState
  type Move
  type PP

  def moveInitialState(move: Move): MoveState

  extension (moveState: MoveState)
    def currentPp(f: PP => PP): MoveState
    def decreasePpBy(value: PP): MoveState
    def increasePpBy(value: PP): MoveState
    def maxPp: PP
    def move: Move

object MoveStateModuleImpl extends MoveStateModule:

  case class Ms(move: Move, currentPp: PP)
  override type MoveState = Ms
  override type Move = scalamon.domain.moves.Move
  override type PP = Int

  override def moveInitialState(move: Move): MoveState =
    Ms(move, move.pp.asInt)

  extension (moveState: MoveState)
    infix def currentPp(f: PP => PP): MoveState =
      val nextPp = f(moveState.currentPp)
      moveState.copy(currentPp = math.max(0, math.min(maxPp, nextPp)))
    infix def decreasePpBy(value: PP): MoveState =
      moveState.copy(currentPp = math.max(0, moveState.currentPp - value))
    infix def increasePpBy(value: PP): MoveState =
      moveState.copy(currentPp = math.min(maxPp, moveState.currentPp + value))
    def maxPp: PP = moveState.move.pp.asInt
    def move: Move = moveState.move