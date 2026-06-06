package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import Accuracy.*

class MoveTest extends org.scalatest.funsuite.AnyFunSuite:

  test("Moves should be of two possible types (damage and status)"):
    """import scalamon.domain.moves.*
      |DamageMove("Thunder", 120, 16, fromPercent(70))""".stripMargin should compile
    """import scalamon.domain.moves.*
      |scalamon.domain.moves.StatusMove("Thunder wave", 0, 32, fromPercent(90))""".stripMargin should compile

  test("Damage moves should have name, power, pp and accuracy"):
    val testedMove: Move = DamageMove("Thunder", 120, 16, fromPercent(70))
    testedMove.name shouldBe "Thunder"
    testedMove.power shouldBe 120
    testedMove.pp shouldBe 16
    testedMove.accuracy shouldBe fromPercent(70)

  test("Move accuracy should not be created with a value different then Accuracy"):
    assertDoesNotCompile("""DamageMove("Thunder", 1.0)""")

  test("Moves should have a name and accuracy"):
    val testedMove: Move = DamageMove("Thunder", 120, 16, fromPercent(70))
    testedMove.name shouldBe "Thunder"
    testedMove.accuracy shouldBe fromPercent(70)
    testedMove.accuracy.asString shouldBe "Accuracy: 70%"

  test("Moves should have an accuracy between 0.0 and 1.0"):
    assertThrows[IllegalArgumentException](DamageMove("Thunder", 120, 16, fromRatio(1.5)))
    assertThrows[IllegalArgumentException](DamageMove("Thunder", 120, 16, fromRatio(-0.5)))
    noException should be thrownBy DamageMove("Thunder", 120, 16, fromRatio(0.7))
