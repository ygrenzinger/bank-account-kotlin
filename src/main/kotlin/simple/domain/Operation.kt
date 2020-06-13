package simple.domain

import java.math.BigDecimal
import java.time.LocalDate

sealed class Operation {
    abstract val amount: BigDecimal
    abstract val date: LocalDate
    abstract fun value(): BigDecimal
    fun applyToBalance(balance: BigDecimal) = balance + value()
}

data class Deposit(override val amount: BigDecimal, override val date: LocalDate) : Operation() {
    override fun value() = amount
}

data class Withdrawal(override val amount: BigDecimal, override val date: LocalDate) : Operation() {
    override fun value() = -amount
}
