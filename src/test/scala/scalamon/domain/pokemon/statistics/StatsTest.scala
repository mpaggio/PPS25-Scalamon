package scalamon.domain.pokemon

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.pokemon.statistics.StatADT.*
import scalamon.domain.pokemon.statistics.Stats

class StatsTest extends AnyFunSuite:

  test("Stats should be created correctly with valid positive values") {
    val myStats = Stats(hp = fromInt(39), attack = fromInt(52), defense = fromInt(43), specialAttack = fromInt(60), specialDefense = fromInt(50), speed = fromInt(65))

    myStats.hp.toInt shouldBe 39
    myStats.attack.toInt shouldBe 52
    myStats.defense.toInt shouldBe 43
    myStats.specialAttack.toInt shouldBe 60
    myStats.specialDefense.toInt shouldBe 50
    myStats.speed.toInt shouldBe 65
  }

  test("Stats creation should fail if HP is 0 or a negative value") {
    a[IllegalArgumentException] should be thrownBy Stats(hp = fromInt(0), attack = fromInt(52), defense = fromInt(43), specialAttack = fromInt(60), specialDefense = fromInt(50), speed = fromInt(65))
  }

  test("Stats creation should fail if at least one of the other statistics is not a positive value") {
    a[IllegalArgumentException] should be thrownBy Stats(hp = fromInt(0), attack = fromInt(52), defense = fromInt(43), specialAttack = fromInt(60), specialDefense = fromInt(50), speed = fromInt(65))
    a[IllegalArgumentException] should be thrownBy Stats(hp = fromInt(39), attack = fromInt(0), defense = fromInt(43), specialAttack = fromInt(60), specialDefense = fromInt(50), speed = fromInt(65))
    a[IllegalArgumentException] should be thrownBy Stats(hp = fromInt(39), attack = fromInt(52), defense = fromInt(0), specialAttack = fromInt(60), specialDefense = fromInt(50), speed = fromInt(65))
    a[IllegalArgumentException] should be thrownBy Stats(hp = fromInt(39), attack = fromInt(52), defense = fromInt(43), specialAttack = fromInt(0), specialDefense = fromInt(50), speed = fromInt(65))
    a[IllegalArgumentException] should be thrownBy Stats(hp = fromInt(39), attack = fromInt(52), defense = fromInt(43), specialAttack = fromInt(60), specialDefense = fromInt(0), speed = fromInt(65))
    a[IllegalArgumentException] should be thrownBy Stats(hp = fromInt(39), attack = fromInt(52), defense = fromInt(43), specialAttack = fromInt(60), specialDefense = fromInt(50), speed = fromInt(0))
  }
