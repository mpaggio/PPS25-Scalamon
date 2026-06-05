package scalamon.logics


object BattleEngine:

  /*def resolve(gameState: GameState, playerActions: (StateExecutor, StateExecutor)): GameState =
    val initialExecutors = List(
      playerActions._1,
      playerActions._2,
    ) ++ gameState.players.flatMap(_.pokemons.flatMap(_.alterateStatus))

    val sortedExecutors = initialExecutors.sortBy(_.priority())

    val finalExecutors = gameState.modifiers.foldLeft(sortedExecutors):
      (executors, modifier) => executors.flatMap(modifier)

    finalExecutors.foldLeft(gameState)((state, executor) => executor.apply()(state))*/