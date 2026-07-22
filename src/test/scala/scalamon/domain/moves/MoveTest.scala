package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import Power.*
import PowerPoints.*
import Accuracy.*
import scalamon.domain.alteredStatus.AlteredStatus.*
import scalamon.domain.moves.StatusMoveCategory.*
import scalamon.domain.moves.DamageMoveCategory.*
import scalamon.domain.types.Type.*

class MoveTest extends org.scalatest.funsuite.AnyFunSuite:
  
  val paralysisEffect = AlteredState(() => Paralyzed, accuracyFromPercent(100))

  test("Moves should have two concrete implementations (damage and status)"):
    """import scalamon.domain.moves.*
      |DamageMove(
      |"Thunder",
      |powerFromInt(120),
      |powerPointsFromInt(16),
      |accuracyFromPercent(70),
      |Electric,
      |Special,
      |None)""".stripMargin should compile
    """import scalamon.domain.moves.*
      |StatusMove(
      |"Thunder wave",
      |powerPointsFromInt(32),
      |accuracyFromPercent(90),
      |Electric,
      |Status,
      |paralysisEffect)""".stripMargin should compile

  test("Damage moves should expose all required fields"):
    val testedMove: DamagingMove = DamageMove(
      "Thunder",
      powerFromInt(120),
      powerPointsFromInt(16),
      accuracyFromPercent(70),
      Electric,
      Special,
      Some(paralysisEffect))
    testedMove.name shouldBe "Thunder"
    testedMove.power shouldBe powerFromInt(120)
    testedMove.pp shouldBe powerPointsFromInt(16)
    testedMove.accuracy shouldBe accuracyFromPercent(70)
    testedMove.moveType shouldBe Electric
    testedMove.category shouldBe Special
    testedMove.effect shouldBe Some(paralysisEffect)

  test("Damaging moves should allow missing effects"):
    val testedNoEffectMove: DamagingMove = DamageMove(
      "Thunder",
      powerFromInt(120),
      powerPointsFromInt(16),
      accuracyFromPercent(70),
      Electric,
      Special,
      None)
    testedNoEffectMove.effect shouldBe None
  
  test("Status moves should expose all required fields"):
    val testedMove: NonDamagingMove = StatusMove(
      "Thunder",
      powerPointsFromInt(16),
      accuracyFromPercent(70),
      Electric,
      Status,
      paralysisEffect)
    testedMove.name shouldBe "Thunder"
    testedMove.pp shouldBe powerPointsFromInt(16)
    testedMove.accuracy shouldBe accuracyFromPercent(70)
    testedMove.moveType shouldBe Electric
    testedMove.category shouldBe Status
    testedMove.effect shouldBe paralysisEffect

  test("Moves should be created using only the domain specific types"):
    assertDoesNotCompile(
      """DamageMove(
        |"Thunder",
        |120,
        |16,
        |70.0,
        |Electric,
        |Special,
        |None)""".stripMargin)
    assertDoesNotCompile(
      """DamageMove(
        |"Thunder",
        |powerFromInt(120),
        |powerPointsFromInt(16),
        |70.0,
        |Electric,
        |Special,
        |None)""".stripMargin)
    assertDoesNotCompile(
      """DamageMove(
        |"Thunder",
        |120,
        |powerPointsFromInt(16),
        |accuracyFromRatio(0.7),
        |Electric,
        |Special,
        |Some(paralysisEffect))""".stripMargin)
    assertDoesNotCompile(
      """DamageMove(
        |"Thunder",
        |powerFromInt(120),
        |16,
        |accuracyFromRatio(0.7),
        |Electric,
        |Special,
        |Some(paralysisEffect))""".stripMargin)
    assertDoesNotCompile(
      """StatusMove(
        |"Thunder wave",
        |32,
        |90,
        |Electric,
        |Status,
        |paralysisEffect)""".stripMargin)
    assertDoesNotCompile(
      """StatusMove(
        |"Thunder wave",
        |powerPointsFromInt(32),
        |90,
        |Electric,
        |Status,
        |paralysisEffect)""".stripMargin)
    assertDoesNotCompile(
      """StatusMove(
        |"Thunder wave",
        |32,
        |accuracyFromPercent(90),
        |Electric,
        |Status,
        |paralysisEffect)""".stripMargin)