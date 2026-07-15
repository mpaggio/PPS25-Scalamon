package scalamon.logics.state

trait StateComponent:
  type SubComponent
  type Modifier = SubComponent => SubComponent