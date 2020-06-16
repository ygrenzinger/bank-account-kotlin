package cqrs.domain.bank

import arrow.core.Either
import cqrs.domain.account.AccountAggregate
import cqrs.domain.common.Aggregate
import cqrs.domain.common.EventStore
import java.util.*

data class BankAggregate(override val aggregateId: UUID,
                         override val eventStore: EventStore,
                         private val accounts: Map<String, UUID> = mapOf())
    : Aggregate<BankAggregate, BankEvent, BankCommand> {
    override val aggregateType: String = "account"

    fun retrieveAccount(accountId: UUID): AccountAggregate? {
        return if (accounts.values.contains(accountId)) {
            loadOrCreateAccountAggregate(accountId)
        } else {
            null
        }
    }

    fun retrieveAccountBySSN(ssn: String) =
            accounts[ssn]?.let { loadOrCreateAccountAggregate(it) }

    private fun loadOrCreateAccountAggregate(accountId: UUID) =
            AccountAggregate.loadAccount(accountId, eventStore) ?: AccountAggregate(accountId, eventStore)

    private fun alreadyExists(createAccount: CreateAccount) =
            accounts.values.contains(createAccount.accountId) || accounts.keys.contains(createAccount.ssn)

    override fun commandToEvents(command: BankCommand) =
            when (command) {
                is CreateAccount -> createAccount(command).map { listOf(it) }
            }

    private fun createAccount(command: CreateAccount): Either<AccountAlreadyExisting, AccountCreated> {
        return if (alreadyExists(command)) {
            Either.left(AccountAlreadyExisting(command))
        } else {
            Either.right(AccountCreated(command.bankId, command.ssn, command.accountId))
        }
    }

    override fun apply(event: BankEvent): BankAggregate =
            when (event) {
                is AccountCreated -> this.copy(accounts = accounts + (event.ssn to event.accountId))
            }

    companion object {
        fun loadBank(aggregateId: UUID, eventStore: EventStore): BankAggregate? {
            return eventStore.rehydrate { BankAggregate(aggregateId, eventStore) }
        }
    }

}
