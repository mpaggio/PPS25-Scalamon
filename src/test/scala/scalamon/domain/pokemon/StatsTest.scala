package scalamon.domain.pokemon

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class StatsTest extends AnyFunSuite:

  test("Stats should be created correctly with valid positive values") {
    val myStats = Stats(hp = 39, attack = 52, defense = 43, specialAttack = 60, specialDefense = 50, speed = 65)

    myStats.hp shouldBe 39
    myStats.attack shouldBe 52
    myStats.defense shouldBe 43
    myStats.specialAttack shouldBe 60
    myStats.specialDefense shouldBe 50
    myStats.speed shouldBe 65
  }

  test("Stats creation should fail if HP is 0 or a negative value") {
    a[IllegalArgumentException] should be thrownBy Stats(hp = 0, attack = 52, defense = 43, specialAttack = 60, specialDefense = 50, speed = 65)
  }

  test("Stats creation should fail if at least one of the other statistics is not a positive value") {
    a[IllegalArgumentException] should be thrownBy Stats(hp = 0, attack = 52, defense = 43, specialAttack = 60, specialDefense = 50, speed = 65)
    a[IllegalArgumentException] should be thrownBy Stats(hp = 39, attack = 0, defense = 43, specialAttack = 60, specialDefense = 50, speed = 65)
    a[IllegalArgumentException] should be thrownBy Stats(hp = 39, attack = 52, defense = 0, specialAttack = 60, specialDefense = 50, speed = 65)
    a[IllegalArgumentException] should be thrownBy Stats(hp = 39, attack = 52, defense = 43, specialAttack = 0, specialDefense = 50, speed = 65)
    a[IllegalArgumentException] should be thrownBy Stats(hp = 39, attack = 52, defense = 43, specialAttack = 60, specialDefense = 0, speed = 65)
    a[IllegalArgumentException] should be thrownBy Stats(hp = 39, attack = 52, defense = 43, specialAttack = 60, specialDefense = 50, speed = 0)
  }
