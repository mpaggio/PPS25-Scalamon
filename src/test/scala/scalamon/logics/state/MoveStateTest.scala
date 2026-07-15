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
    val updatedState = state decreasePpBy 10
    updatedState.currentPp shouldBe (swift.pp.asInt - 10)

  test("Decrease PP should not let current PP go below zero"):
    val state = moveInitialState(swift)
    val updatedState = state decreasePpBy 40
    updatedState.currentPp shouldBe 0

  test("Increase PP should correctly increase current PP"):
    val state = moveInitialState(swift).currentPp(_ => 10)
    val updatedState = state increasePpBy 5
    updatedState.currentPp shouldBe 15

  test("Increase PP should not let current PP exceed maximum PP"):
    val state = moveInitialState(swift)
    val updatedState = state increasePpBy 5
    updatedState.currentPp shouldBe swift.pp.asInt

  test("Current PP transformer should apply clamping logic"):
    val state = moveInitialState(swift).currentPp(_ => 10)
    val underflowState = state currentPp (_ - 37)
    underflowState.currentPp shouldBe 0
    val overflowState = state currentPp (_ + 100)
    overflowState.currentPp shouldBe swift.pp.asInt

  test("Move state transformations should be referentially transparent"):
    val state = moveInitialState(swift)
    val state2 = state decreasePpBy 5
    state.currentPp shouldBe swift.pp.asInt
    state2.currentPp shouldBe (swift.pp.asInt - 5)