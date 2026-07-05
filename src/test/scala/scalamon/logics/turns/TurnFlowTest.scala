package scalamon.logics.turns

import org.scalatest.funsuite.AnyFunSuite
import scalamon.logics.turns.BattleAction.{SwitchPokemon, UseMove}
import scalamon.logics.turns.ActionOrderResolver.priority

class TurnFlowTest extends AnyFunSuite:

  test("ActionOrderResolver orders actions by priority descending"):
    val actions = List(
      ScheduledAction(
        UseMove(
          trainerId = TrainerId("p1"),
          attacking = PokemonRef("pikachu"),
          defending = PokemonRef("bulbasaur"),
          move = MoveRef("thunderbolt"),
          priority = 0
        ),
        speed = Speed(100)
      ),
      ScheduledAction(
        SwitchPokemon(
          trainerId = TrainerId("p2"),
          from = PokemonRef("charmander"),
          to = PokemonRef("squirtle"),
          priority = 6
        ),
        speed = Speed(50)
      )
    )
    val ordered = ActionOrderResolver.default.order(actions)
    assert(ordered.head.action == actions(1).action)

  test("ActionOrderResolver orders action by speed if priority is equal"):
    val slow = ScheduledAction(
      UseMove(
        trainerId = TrainerId("p1"),
        attacking = PokemonRef("bulbasaur"),
        defending = PokemonRef("pikachu"),
        move = MoveRef("vine-whip"),
        priority = 0
      ),
      speed = Speed(50)
    )
    val fast = ScheduledAction(
      UseMove(
        trainerId = TrainerId("p2"),
        attacking = PokemonRef("pikachu"),
        defending = PokemonRef("bulbasaur"),
        move = MoveRef("quick-attack"),
        priority = 0
      ),
      speed = Speed(90)
    )
    val ordered = ActionOrderResolver.default.order(List(slow, fast))
    assert(ordered == List(fast, slow))

  test("TurnFlow builds and orders scheduled actions from player choises"):
    val flow = TurnFlow(ActionOrderResolver.default)
    val choises = TurnChoices(
      first = UseMove(
        trainerId = TrainerId("p1"),
        attacking = PokemonRef("bulbasaur"),
        defending = PokemonRef("pikachu"),
        move = MoveRef("tackle"),
        priority = 0
      ),
      second = UseMove(
        trainerId = TrainerId("p2"),
        attacking = PokemonRef("pikachu"),
        defending = PokemonRef("bulbasaur"),
        move = MoveRef("quick-attack"),
        priority = 1
      )
    )
    val speedOf: PokemonRef => Speed = ref =>
      if ref.value == "pikachu" then Speed(90)
      else Speed(45)
    val plan = flow.actionOrdering(choises, speedOf)
    assert(plan.orderedActions.head.action == choises.second)

  test("BattleAction priority returns the priority of a move action"):
    val action =
      UseMove(
        trainerId = TrainerId("p1"),
        attacking = PokemonRef("pikachu"),
        defending = PokemonRef("bulbasaur"),
        move = MoveRef("quick-attack"),
        priority = 1
      )
    assert(action.priority == 1)

  test("BattleAction priority returns the priority of a switch action")
  val action =
    SwitchPokemon(
      trainerId = TrainerId("p1"),
      from = PokemonRef("pikachu"),
      to = PokemonRef("bulbasaur"),
      priority = 6
    )
  assert(action.priority == 6)
