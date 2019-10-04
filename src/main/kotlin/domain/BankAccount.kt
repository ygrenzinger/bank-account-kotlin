package domain

import java.math.BigDecimal
import java.time.LocalDate

class BankAccount(private val operations: MutableList<Operation>) {

    val balance: BigDecimal
        get() = operations.fold(BigDecimal(0)) { balance, operation -> operation.applyToBalance(balance) }

    fun deposit(amount: BigDecimal, date: LocalDate = LocalDate.now()): BankAccount {
        operations.add(Deposit(amount, date))
        return this
    }

    fun withdraw(amount: BigDecimal, date: LocalDate = LocalDate.now()): BankAccount {
        operations.add(Withdrawal(amount, date))
        return this
    }

    fun transferTo(amount: BigDecimal, otherBankAccount: BankAccount, localDate: LocalDate = LocalDate.now()) {
        this.withdraw(amount, localDate)
        otherBankAccount.deposit(amount, localDate)
    }

    fun retrieveStatementLines() = operations.fold(
            listOf<StatementLine>(),
            { lines, curOp ->
                val lastBalance = lines.lastOrNull()?.balance ?: BigDecimal.ZERO
                lines + StatementLine(curOp, lastBalance + curOp.value())
            }
    )

    companion object {
        fun create(initialAmount: BigDecimal, date: LocalDate = LocalDate.now()) =
                BankAccount(mutableListOf(Deposit(initialAmount, date)))
    }

}
