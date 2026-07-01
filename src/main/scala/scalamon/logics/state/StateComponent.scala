package scalamon.logics.state

trait StateComponent:
  protected type State
  protected type Op = State => State
  protected type InnerState
  protected type InnerOp = InnerState => InnerState
