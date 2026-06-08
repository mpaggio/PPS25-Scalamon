package scalamon.domain.pokemon.abilities

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.pokemon.abilities.{Ability, AbilitySlot}

class AbilitySlotTest extends AnyFunSuite:

  test("AbilitySlot memorizes 1 primary ability and optionally 1 hidden ability") {
    val mySlot = AbilitySlot(primary = Ability.Blaze, hidden = Some(Ability.SolarPower))

    mySlot.primary shouldBe Ability.Blaze
    mySlot.secondary shouldBe empty
    mySlot.hidden shouldBe Some(Ability.SolarPower)
  }

  test("AbilitySlot memorizes 1 secondary ability if provided, in addition to the primary ability") {
    val mySlot2 = AbilitySlot(primary = Ability.EffectSpore, secondary = Some(Ability.Regenerator))

    mySlot2.primary shouldBe Ability.EffectSpore
    mySlot2.primary shouldNot be (Ability.Blaze)
    mySlot2.secondary shouldBe Some(Ability.Regenerator)
    mySlot2.hidden shouldBe empty
  }

  test("AbilitySlot is well-created even if only the primary ability is provided") {
    val mySlot3 = AbilitySlot(primary = Ability.Levitate)

    mySlot3.primary shouldBe Ability.Levitate
    mySlot3.secondary shouldBe empty
    mySlot3.hidden shouldBe empty
  }

  test("AbilitySlot cannot be created if all 3 abilities are provided") {
    a[IllegalArgumentException] should be thrownBy AbilitySlot(primary = Ability.Blaze, secondary = Some(Ability.Regenerator), hidden = Some(Ability.SolarPower))
  }

  test("AbilitySlot with the same configuration of abilities are equal") {
    val mySlot4 = AbilitySlot(primary = Ability.Blaze, hidden = Some(Ability.SolarPower))
    val mySlot5 = AbilitySlot(primary = Ability.Blaze, hidden = Some(Ability.SolarPower))

    mySlot4 shouldEqual mySlot5
  }