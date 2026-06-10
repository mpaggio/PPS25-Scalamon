package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import Power.*
import PowerPoints.*
import Accuracy.*
import MoveEffect.*
import scalamon.domain.moves.StatusMoveCategory.*
import scalamon.domain.moves.DamageMoveCategory.*
import scalamon.domain.types.Type.*
import scalamon.domain.pokemon.statistics.StatADT.StatKind.*

class MoveTest extends org.scalatest.funsuite.AnyFunSuite:
  
  val paralysisEffect = AlteredState(Paralysis, accuracyFromPercent(100))
  val statDropEffect = StatChange(Speed, -1, accuracyFromPercent(10))

  test("Moves should be of two possible types (damage and status)"):
    """import scalamon.domain.moves.*
      |DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), accuracyFromPercent(70), Electric, Special, None)""".stripMargin should compile
    """import scalamon.domain.moves.*
      |scalamon.domain.moves.StatusMove("Thunder wave", powerPointsFromInt(32), accuracyFromPercent(90), Electric, Status, paralysisEffect)""".stripMargin should compile

  test("Damage moves should have name, power, pp, accuracy, type, category and an optional effect"):
    val testedMove: DamagingMove = DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), accuracyFromPercent(70), Electric, Special, Some(paralysisEffect))
    testedMove.name shouldBe "Thunder"
    testedMove.power shouldBe powerFromInt(120)
    testedMove.pp shouldBe powerPointsFromInt(16)
    testedMove.accuracy shouldBe accuracyFromPercent(70)
    testedMove.moveType shouldBe Electric
    testedMove.category shouldBe Special
    testedMove.effect shouldBe Some(paralysisEffect)
    val testedNoEffectMove: DamagingMove = DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), accuracyFromPercent(70), Electric, Special, None)
    testedNoEffectMove.effect shouldBe None
  
  test("Status moves should have a name, pp, accuracy, type, category and effect"):
    val testedMove: NonDamagingMove = StatusMove("Thunder", powerPointsFromInt(16), accuracyFromPercent(70), Electric, Status, paralysisEffect)
    testedMove.name shouldBe "Thunder"
    testedMove.pp.asString shouldBe "PP: 16"
    testedMove.accuracy.asString shouldBe "Accuracy: 70%"
    testedMove.moveType shouldBe Electric
    testedMove.category shouldBe Status
    testedMove.effect shouldBe paralysisEffect

  test("Move accuracy should not be created without a value of Power, PP, Accuracy, Type, Category and Effect (effect optional for damage moves)"):
    assertDoesNotCompile("""DamageMove("Thunder", 120, 16, 70.0, Electric, Special, None)""")
    assertDoesNotCompile("""DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), 70.0, Electric, Special, None)""")
    assertDoesNotCompile("""DamageMove("Thunder", 120, powerPointsFromInt(16), accuracyFromRatio(0.7), Electric, Special, Some(paralysisEffect))""")
    assertDoesNotCompile("""DamageMove("Thunder", powerFromInt(120), 16, accuracyFromRatio(0.7), Electric, Special, Some(paralysisEffect))""")
    assertDoesNotCompile("""StatusMove("Thunder wave", 32, 90, Electric, Status, paralysisEffect)""")
    assertDoesNotCompile("""StatusMove("Thunder wave", powerPointsFromInt(32), 90, Electric, Status, paralysisEffect)""")
    assertDoesNotCompile("""StatusMove("Thunder wave", 32, accuracyFromPercent(90), Electric, Status, paralysisEffect)""")
    
  test("Moves should be created only with stats valid values"):
    assertThrows[IllegalArgumentException](DamageMove("Thunder", powerFromInt(251), powerPointsFromInt(67), accuracyFromRatio(1.5), Electric, Special, None))
    assertThrows[IllegalArgumentException](StatusMove("Thunder wave", powerPointsFromInt(0), accuracyFromRatio(-0.5), Electric, Status, paralysisEffect))
    noException should be thrownBy DamageMove("Thunder", powerFromInt(120), powerPointsFromInt(16), accuracyFromRatio(0.7), Electric, Special, Some(paralysisEffect))
