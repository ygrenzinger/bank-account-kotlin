package cqrs.domain.bank

import arrow.core.Either
import arrow.core.Option
import cqrs.domain.account.AccountAggregate
import cqrs.domain.common.Aggregate
import cqrs.domain.common.EventStore
import java.util.*

class BankAggregate(aggregateId: UUID, eventStore: EventStore)
    : Aggregate<BankAggregate, BankEvent, BankCommand>(aggregateId, AccountAggregate.TYPE, eventStore) {

    private val accounts = mutableSetOf<UUID>()

    fun retrieveAccount(accountId: UUID) : Option<AccountAggregate> {
        return if (accounts.contains(accountId)) {
            Option.just(AccountAggregate.loadAccount(accountId, eventStore))
        } else {
            Option.empty()
        }
    }

    override fun commandToEvents(command: BankCommand): Either<Exception, List<BankEvent>> {
        return try {
            return when (command) {
                is CreateAccount -> if (!accounts.contains(command.accountId)) {
                    Either.right(listOf(AccountCreated(command.bankId, command.accountId)))
                } else {
                    Either.left(ExistingAccountException(command.accountId))
                }
            }
        } catch (e: Exception) {
            Either.left(e)
        }
    }

    override fun applyEvent(event: BankEvent): BankAggregate {
        when (event) {
            is AccountCreated -> accounts.add(event.accountId)
        }
        return this
    }

}
