package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import Power.*
import PowerPoints.*
import Accuracy.*
import scalamon.domain.moves.StatusMoveCategory.*
import scalamon.domain.moves.DamageMoveCategory.*
import scalamon.domain.types.Type.*

class MoveTest extends org.scalatest.funsuite.AnyFunSuite:

  test("Moves should be of two possible types (damage and status)"):
    """import scalamon.domain.moves.*
      |DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), accuracyFromPercent(70), Electric, Special)""".stripMargin should compile
    """import scalamon.domain.moves.*
      |scalamon.domain.moves.StatusMove("Thunder wave", powerPointsFromInt(32), accuracyFromPercent(90), Electric, Status)""".stripMargin should compile

  test("Damage moves should have name, power, pp and accuracy"):
    val testedMove: DamagingMove = DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), accuracyFromPercent(70), Electric, Special)
    testedMove.name shouldBe "Thunder"
    testedMove.power shouldBe powerFromInt(120)
    testedMove.pp shouldBe powerPointsFromInt(16)
    testedMove.accuracy shouldBe accuracyFromPercent(70)
    testedMove.moveType shouldBe Electric
    testedMove.category shouldBe Special

  test("Status moves should have a name, pp and accuracy"):
    val testedMove: NonDamagingMove = StatusMove("Thunder", powerPointsFromInt(16), accuracyFromPercent(70), Electric, Status)
    testedMove.name shouldBe "Thunder"
    testedMove.pp.asString shouldBe "PP: 16"
    testedMove.accuracy.asString shouldBe "Accuracy: 70%"
    testedMove.moveType shouldBe Electric
    testedMove.category shouldBe Status

  test("Move accuracy should not be created without a value of type Power, a value of type PP and a value of type Accuracy (only PP and Accuracy for status moves)"):
    assertDoesNotCompile("""DamageMove("Thunder", 120, 16, 70.0, Electric, Special)""")
    assertDoesNotCompile("""DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), 70.0, Electric, Special)""")
    assertDoesNotCompile("""DamageMove("Thunder", 120, powerPointsFromInt(16), accuracyFromRatio(0.7), Electric, Special)""")
    assertDoesNotCompile("""DamageMove("Thunder", powerFromInt(120), 16, accuracyFromRatio(0.7), Electric, Special)""")
    assertDoesNotCompile("""StatusMove("Thunder wave", 32, 90, Electric, Status)""")
    assertDoesNotCompile("""StatusMove("Thunder wave", powerPointsFromInt(32), 90, Electric, Status)""")
    assertDoesNotCompile("""StatusMove("Thunder wave", 32, accuracyFromPercent(90), Electric, Status)""")
    
  test("Moves should be created only with stats valid values"):
    assertThrows[IllegalArgumentException](DamageMove("Thunder", powerFromInt(251), powerPointsFromInt(67), accuracyFromRatio(1.5), Electric, Special))
    assertThrows[IllegalArgumentException](StatusMove("Thunder wave", powerPointsFromInt(0), accuracyFromRatio(-0.5), Electric, Status))
    noException should be thrownBy DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), accuracyFromRatio(0.7), Electric, Special)
