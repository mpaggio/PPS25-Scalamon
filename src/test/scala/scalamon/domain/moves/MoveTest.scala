package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import Accuracy.*
import Power.*
import PowerPoints.*

class MoveTest extends org.scalatest.funsuite.AnyFunSuite:

  test("Moves should be of two possible types (damage and status)"):
    """import scalamon.domain.moves.*
      |DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), accuracyFromPercent(70))""".stripMargin should compile
    """import scalamon.domain.moves.*
      |scalamon.domain.moves.StatusMove("Thunder wave", powerFromInt(0), powerPointsFromInt(32), accuracyFromPercent(90))""".stripMargin should compile

  test("Damage moves should have name, power, pp and accuracy"):
    val testedMove: Move = DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), accuracyFromPercent(70))
    testedMove.name shouldBe "Thunder"
    testedMove.power shouldBe powerFromInt(120)
    testedMove.pp shouldBe powerPointsFromInt(16)
    testedMove.accuracy shouldBe accuracyFromPercent(70)

  test("Status moves should have a name, power, pp and accuracy"):
    val testedMove: Move = StatusMove("Thunder", powerFromInt(120), powerPointsFromInt(16), accuracyFromPercent(70))
    testedMove.name shouldBe "Thunder"
    testedMove.power.asString shouldBe "Power: 120"
    testedMove.pp.asString shouldBe "PP: 16"
    testedMove.accuracy.asString shouldBe "Accuracy: 70%"

  test("Move accuracy should not be created without a value of type Power, a value of type PP and a value of type Accuracy"):
    assertDoesNotCompile("""DamageMove("Thunder", 120, 16, 70.0)""")
    assertDoesNotCompile("""DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), 70.0)""")
    assertDoesNotCompile("""DamageMove("Thunder", 120, powerPointsFromInt(16), accuracyFromRatio(0.7))""")
    assertDoesNotCompile("""DamageMove("Thunder", powerFromInt(120), 16, accuracyFromRatio(0.7))""")

  test("Moves should be created only with stats valid values"):
    assertThrows[IllegalArgumentException](DamageMove("Thunder", powerFromInt(251), powerPointsFromInt(67), accuracyFromRatio(1.5)))
    assertThrows[IllegalArgumentException](DamageMove("Thunder", powerFromInt(0), powerPointsFromInt(0), accuracyFromRatio(-0.5)))
    noException should be thrownBy DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), accuracyFromRatio(0.7))
