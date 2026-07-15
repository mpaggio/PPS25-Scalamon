package scalamon.domain.moves

import scalamon.domain.types.Type.*
import DamageMoveCategory.*
import MoveDatabase.*

class MoveDatabaseTest extends org.scalatest.funsuite.AnyFunSuite:

  test("Set of moves should not be empty"):
    assert(MoveDatabase.allMoves.nonEmpty)

  test("Damaging moves should contain only DamagingMove instances"):
    assert(MoveDatabase.damagingMoves.forall(_.isInstanceOf[DamagingMove]))

  test("Non damaging moves should contain only NonDamagingMove instances"):
    assert(MoveDatabase.nonDamagingMoves.forall(_.isInstanceOf[NonDamagingMove]))

  test("Damaging moves and non damaging moves should partition all moves"):
    val total: Int = MoveDatabase.damagingMoves.size + MoveDatabase.nonDamagingMoves.size
    assert(total == MoveDatabase.allMoves.size)

  test("Of type should return only moves of the requested type"):
    val fireMoves = MoveDatabase.allMoves.ofType(Fire)
    assert(fireMoves.nonEmpty)
    assert(fireMoves.forall(_.moveType == Fire))

  test("Find by name should find an existing move"):
    val move = MoveDatabase.allMoves.findByName("Flamethrower")
    assert(move.isDefined)
    assert(move.get.name == "Flamethrower")

  test("Find by name should be case sensitive"):
    val move = MoveDatabase.allMoves.findByName("fLaMeThRoWeR")
    assert(move.isDefined)
    assert(move.get.name == "Flamethrower")

  test("Find by name should return None for unknown moves"):
    assert(MoveDatabase.allMoves.findByName("NotExistingMove").isEmpty)

  test("Of category should return only move of that damaging category"):
    val specialMoves = MoveDatabase.allMoves.ofCategory(Special)
    assert(specialMoves.nonEmpty)
    assert(specialMoves.forall(_.category == Special))
    val physicalMoves = MoveDatabase.allMoves.ofCategory(Physical)
    assert(physicalMoves.nonEmpty)
    assert(physicalMoves.forall(_.category == Physical))

  test("There should be at least one move for each represented type"):
    assert(MoveDatabase.allMoves.ofType(Normal).nonEmpty)
    assert(MoveDatabase.allMoves.ofType(Fire).nonEmpty)
    assert(MoveDatabase.allMoves.ofType(Water).nonEmpty)
    assert(MoveDatabase.allMoves.ofType(Grass).nonEmpty)
    assert(MoveDatabase.allMoves.ofType(Electric).nonEmpty)
    assert(MoveDatabase.allMoves.ofType(Psychic).nonEmpty)
    assert(MoveDatabase.allMoves.ofType(Poison).nonEmpty)