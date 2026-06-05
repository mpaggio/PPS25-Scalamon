package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import Accuracy.*

class MoveTest extends org.scalatest.funsuite.AnyFunSuite:

  test("Move accuracy should not be created with a value different then Accuracy"):
    assertDoesNotCompile("""DamageMove("Thunder", 1.0)""")

  test("Moves should have a name and accuracy"):
    val testedMove: Move = DamageMove("Thunder", fromRatio(1.0))
    testedMove.name shouldBe "Thunder"
    testedMove.accuracy.asString shouldBe "Accuracy: 100%"

  test("Moves should have an accuracy between 0.0 and 1.0"):
    assertThrows[IllegalArgumentException](DamageMove("Thunder", fromRatio(1.5)))
    assertThrows[IllegalArgumentException](DamageMove("Thunder", fromRatio(-0.5)))
    noException should be thrownBy DamageMove("Thunder", fromRatio(0.7))
