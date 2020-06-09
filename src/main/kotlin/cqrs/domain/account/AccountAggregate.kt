package cqrs.domain.account

import arrow.core.Either
import cqrs.domain.common.Aggregate
import cqrs.domain.common.EventStore
import cqrs.domain.common.Money
import java.util.*

class AccountAggregate(aggregateId: UUID, eventStore: EventStore)
    : Aggregate<AccountAggregate, AccountEvent, AccountCommand>(aggregateId, TYPE, eventStore) {

    var balance: Money = Money.zero
        private set

    override fun commandToEvents(command: AccountCommand): Either<Exception, List<AccountEvent>> =
            Either.right(when (command) {
                is MakeDeposit -> listOf(DepositMade(command.accountId, command.amount))
                is MakeWithdraw -> listOf(WithdrawMade(command.accountId, command.amount))
            })

    override fun applyEvent(event: AccountEvent): AccountAggregate {
        when (event) {
            is DepositMade -> balance += event.amount
            is WithdrawMade -> balance -= event.amount
        }
        return this
    }

    companion object {
        const val TYPE = "account"
        fun loadAccount(aggregateId: UUID, eventStore: EventStore): AccountAggregate {
            val account = AccountAggregate(aggregateId, eventStore)
            account.rehydrate()
            return account
        }
    }
}
