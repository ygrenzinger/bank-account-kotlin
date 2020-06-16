package cqrs.domain.account.view

import cqrs.domain.account.AccountAggregate
import cqrs.domain.account.AccountEvent
import cqrs.domain.account.DepositMade
import cqrs.domain.account.WithdrawMade
import cqrs.domain.common.Money
import cqrs.domain.common.View

class StatementHistory(override val associatedAggregate: AccountAggregate) : View<AccountAggregate, AccountEvent> {

    private var balance = Money.zero
    private val lines = mutableListOf<StatementLine>()
    private val reversed = lines.asReversed()

    fun lines(): List<String> {
        return listOf("date  | amount | balance") + reversed.map { it.toPrint() }
    }

    override fun apply(event: AccountEvent) {
        if (event.aggregateIdentifier() == associatedAggregate.aggregateId) {
            when (event) {
                is DepositMade -> {
                    lines.add(StatementLine(event.date, event.amount, balance))
                    balance += event.amount
                }
                is WithdrawMade -> {
                    StatementLine(event.date, event.amount, balance)
                    lines.add(StatementLine(event.date, -event.amount, balance))
                    balance -= event.amount
                }
            }
        }
    }

    override fun selecting(): String {
        return AccountEvent.TYPE
    }
}