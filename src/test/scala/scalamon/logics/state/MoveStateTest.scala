package scalamon.logics.state

import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.types.Type
import scalamon.domain.types.Type.*
import scalamon.domain.moves.MoveDSL.{move, *}
import scalamon.domain.moves.DamagingMove
import scalamon.domain.moves.DamageMoveCategory.*
import scalamon.logics.state.MoveStateModuleImpl.*

class MoveStateTest extends org.scalatest.funsuite.AnyFunSuite:

  val swift: DamagingMove = move named "Swift" withPower 60 withPP 32 withAccuracy 100 withType Normal as Special

  test("Move initial state should initialize current PP to maximum PP"):
    val state = moveInitialState(swift)
    state.currentPp shouldBe swift.pp.asInt
    state.maxPp shouldBe swift.pp.asInt
    state.move shouldBe swift

  test("Decrease PP should correctly reduce current PP"):
    val state = moveInitialState(swift)
    val updatedState = decreasePpBy(10)(state)
    updatedState.currentPp shouldBe (swift.pp.asInt - 10)

  test("Decrease PP should not let current PP go below zero"):
    val state = moveInitialState(swift)
    val updatedState = decreasePpBy(40)(state)
    updatedState.currentPp shouldBe 0

  test("Increase PP should correctly increase current PP"):
    val state = currentPp(_ => 10)(moveInitialState(swift))
    val updatedState = increasePpBy(5)(state)
    updatedState.currentPp shouldBe 15

  test("Increase PP should not let current PP exceed maximum PP"):
    val state = moveInitialState(swift)
    val updatedState = increasePpBy(5)(state)
    updatedState.currentPp shouldBe swift.pp.asInt

  test("Current PP transformer should apply clamping logic"):
    val state = currentPp(_ => 10)(moveInitialState(swift))
    val underflowState = currentPp(_ - 37)(state)
    underflowState.currentPp shouldBe 0
    val overflowState = currentPp(_ + 100)(state)
    overflowState.currentPp shouldBe swift.pp.asInt

  test("Move state transformations should be referentially transparent"):
    val state = moveInitialState(swift)
    val state2 = decreasePpBy(5)(state)
    state.currentPp shouldBe swift.pp.asInt
    state2.currentPp shouldBe (swift.pp.asInt - 5)

  test("AccuracyPercent transformer should correctly decrement move accuracy"):
    val state = moveInitialState(swift)
    val updatedState = accuracyPercent(_ - 20)(state)
    updatedState.move.accuracy.asInt shouldBe 80
    state.move.accuracy.asInt shouldBe 100

  test("AccuracyPercent transformer should correctly increment move accuracy"):
    val state = moveInitialState(swift)
    val updatedState = accuracyPercent(_ - 30 + 20)(state)
    updatedState.move.accuracy.asInt shouldBe 90
    state.move.accuracy.asInt shouldBe 100

  test("AccuracyPercent transformer should clamp move accuracy between 0 and 100"):
    val state = moveInitialState(swift)
    val overflowState = accuracyPercent(_ + 20)(state)
    overflowState.move.accuracy.asInt shouldBe 100
    val underflowState = accuracyPercent(_ - 120)(state)
    underflowState.move.accuracy.asInt shouldBe 0