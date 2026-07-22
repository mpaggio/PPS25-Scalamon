package scalamon.domain.pokemon.abilities

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.pokemon.abilities.Ability.*


class AbilitySlotTest extends AnyFunSuite:

  test("AbilitySlot memorizes 1 primary ability and optionally 1 hidden ability") {
    val mySlot = AbilitySlot(primary = Blaze, hidden = Some(SolarPower))

    mySlot.primary shouldBe Blaze
    mySlot.secondary shouldBe empty
    mySlot.hidden shouldBe Some(SolarPower)
  }

  test("AbilitySlot memorizes 1 secondary ability if provided, in addition to the primary ability") {
    val mySlot2 = AbilitySlot(primary = EffectSpore, secondary = Some(Regenerator))

    mySlot2.primary shouldBe EffectSpore
    mySlot2.primary shouldNot be (Blaze)
    mySlot2.secondary shouldBe Some(Regenerator)
    mySlot2.hidden shouldBe empty
  }

  test("AbilitySlot is well-created even if only the primary ability is provided") {
    val mySlot3 = AbilitySlot(primary = Levitate)

    mySlot3.primary shouldBe Levitate
    mySlot3.secondary shouldBe empty
    mySlot3.hidden shouldBe empty
  }

  test("AbilitySlot cannot be created if all 3 abilities are provided") {
    a[IllegalArgumentException] should be thrownBy AbilitySlot(primary = Blaze, secondary = Some(Regenerator), hidden = Some(SolarPower))
  }

  test("AbilitySlot with the same configuration of abilities are equal") {
    val mySlot4 = AbilitySlot(primary = Blaze, hidden = Some(SolarPower))
    val mySlot5 = AbilitySlot(primary = Blaze, hidden = Some(SolarPower))

    mySlot4 shouldEqual mySlot5
  }