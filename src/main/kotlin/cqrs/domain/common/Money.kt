package cqrs.domain.common

import java.math.BigDecimal

data class Money(val amount: BigDecimal) {

    operator fun plus(b: Money) = Money(this.amount + b.amount)
    operator fun minus(b: Money) = Money(this.amount - b.amount)
    operator fun unaryMinus() = Money(-this.amount)

    companion object {
        fun of(value: Double) = Money(BigDecimal.valueOf(value))
        val zero = of(0.0)
    }
}