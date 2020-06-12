package cqrs.domain.account.view

import cqrs.domain.account.AccountEvent
import cqrs.domain.account.DepositMade
import cqrs.domain.account.WithdrawMade
import cqrs.domain.common.Event
import cqrs.domain.common.EventBus
import cqrs.domain.common.Money
import java.util.*

class StatementHistory(private val accountId: UUID) {

    private var balance = Money.zero
    private val lines = mutableListOf<StatementLine>()
    private val reversed = lines.asReversed()

    fun attachToBus(eventBus: EventBus) {
        eventBus.attach { event ->
            consume(event)
        }
    }

    fun lines(): List<String> {
        return listOf("date  | amount | balance") +  lines.reversed().map { it.toPrint() }
    }

    private fun consume(event: Event) {
        if (event.aggregateIdentifier() == accountId && event is AccountEvent) {
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
}