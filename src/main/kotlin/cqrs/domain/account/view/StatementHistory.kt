package cqrs.domain.account.view

import cqrs.domain.account.*
import cqrs.domain.common.Money
import cqrs.domain.common.View
import java.time.LocalDate

class StatementHistory(override val associatedAggregate: AccountAggregate) : View<AccountAggregate, AccountEvent> {

    private var balance = Money.zero
    private val lines = mutableListOf<StatementLine>()
    private val reversed = lines.asReversed()

    fun lines(): List<String> {
        return listOf("|    date    | amount | balance | label |") + reversed.map { it.toPrint() }
    }

    override fun apply(event: AccountEvent) {
        if (event.aggregateIdentifier() == associatedAggregate.aggregateId) {
            when (event) {
                is DepositMade -> addStatementLine(event.date, event.amount)
                is WithdrawMade -> addStatementLine(event.date, -event.amount)
                is TransferWithdrawMade -> addStatementLine(event.date, -event.amount, "Transfer ${event.transferId}")
                is TransferDepositMade -> addStatementLine(event.date, event.amount, "Transfer ${event.transferId}")
                is TransferWithdrawCanceled -> addStatementLine(event.date, -event.amount, "Transfer ${event.transferId} cancelled")
            }
        }
    }

    private fun addStatementLine(date: LocalDate, amount: Money, label: String = "") {
        balance = lines.lastOrNull()?.balance ?: Money.zero
        lines.add(StatementLine(date, amount, balance + amount, label))
    }

    override fun selecting(): String {
        return AccountEvent.TYPE
    }
}