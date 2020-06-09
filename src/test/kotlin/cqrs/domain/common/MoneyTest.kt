package cqrs.domain.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MoneyTest : StringSpec({
    "should add money" {
        (Money.of(100.0) + Money.of(25.0)) shouldBe Money.of(125.0)
    }
    "should subtract money" {
        (Money.of(100.0) - Money.of(25.0)) shouldBe Money.of(75.0)
    }
})