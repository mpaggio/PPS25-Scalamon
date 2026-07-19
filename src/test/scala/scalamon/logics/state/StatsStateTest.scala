package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.StatsStateModuleImpl.*

class StatsStateTest extends AnyWordSpec with Matchers:

  private val stats: StatsState =
    Ss(hp = 39, attack = 52, defense = 43, specialAttack = 60, specialDefense = 50, speed = 65)

  "StatsState lifters" should:

    "modify only the attack stat" in:
      attack(_ - 10)(stats).attack shouldEqual 42

    "modify only the defense stat" in:
      defense(_ + 2)(stats).defense shouldEqual 45

    "modify only the special attack stat" in:
      specialAttack(_ + 5)(stats).specialAttack shouldEqual 65

    "modify only the special defense stat" in:
      specialDefense(_ - 5)(stats).specialDefense shouldEqual 45

    "modify only the speed stat" in:
      speed(_ * 2)(stats).speed shouldEqual 130

  "Stat inner transformers" should:

    "decrease by the given amount" in:
      decrease(10)(52) shouldEqual 42

    "increase by the given amount" in:
      increase(10)(52) shouldEqual 62

    "multiply truncating to Int" in:
      multiply(0.5)(65) shouldEqual 32
      multiply(1.5)(43) shouldEqual 64

    "compose" in:
      val modifier = attack(decrease(2)) andThen defense(increase(2)) andThen speed(multiply(0.5))
      val s = modifier(stats)
      s.attack shouldEqual 50
      s.defense shouldEqual 45
      s.speed shouldEqual 32

  "Stat extensions" should:

    "clamp a value inside the given bounds" in:
      50.clamped(0, 100) shouldEqual 50
      -5.clamped(0, 100) shouldEqual 0
      120.clamped(0, 100) shouldEqual 100

    "force a value to be non negative" in:
      5.positive shouldEqual 5
      -5.positive shouldEqual 0
