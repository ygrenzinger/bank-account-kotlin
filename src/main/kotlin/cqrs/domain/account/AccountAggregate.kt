package cqrs.domain.account

import arrow.core.Either
import cqrs.domain.common.Aggregate
import cqrs.domain.common.EventStore
import cqrs.domain.common.Money
import java.util.*

data class AccountAggregate(override val aggregateId: UUID,
                            override val eventStore: EventStore,
                            val balance: Money = Money.zero)
    : Aggregate<AccountAggregate, AccountEvent, AccountCommand> {
    override val aggregateType: String = "account"

    override fun commandToEvents(command: AccountCommand): Either<Exception, List<AccountEvent>> =
            when (command) {
                is MakeDeposit -> Either.right(listOf(DepositMade(command.accountId, command.amount, command.date)))
                is MakeWithdraw -> withdrawMoney(command.amount) { listOf(WithdrawMade(command.accountId, command.amount, command.date)) }
                is MakeTransferWithdraw -> withdrawMoney(command.amount) { listOf(TransferWithdrawMade(command.accountId, command.transferId, command.amount, command.date)) }
                is MakeTransferDeposit -> Either.right(listOf(TransferDepositMade(command.accountId, command.transferId, command.amount, command.date)))
                is CancelTransferWithdraw -> Either.right(listOf(TransferWithdrawCanceled(command.accountId, command.transferId, command.amount)))
            }

    private fun withdrawMoney(money: Money, f: () -> List<AccountEvent>): Either<Exception, List<AccountEvent>> {
        val remaining = balance - money
        return if (remaining < Money.zero) {
            Either.left(NotEnoughMoney(-remaining))
        } else {
            Either.right(f())
        }
    }

    override fun apply(event: AccountEvent): AccountAggregate =
            when (event) {
                is DepositMade -> this.copy(balance = balance + event.amount)
                is WithdrawMade -> this.copy(balance = balance - event.amount)
                is TransferDepositMade -> this.copy(balance = balance + event.amount)
                is TransferWithdrawMade -> this.copy(balance = balance - event.amount)
                is TransferWithdrawCanceled -> this.copy(balance = balance + event.amount)
            }

    companion object {
        fun loadAccount(aggregateId: UUID, eventStore: EventStore): AccountAggregate {
            val account = AccountAggregate(aggregateId, eventStore)
            account.rehydrate()
            return account
        }
    }

}
