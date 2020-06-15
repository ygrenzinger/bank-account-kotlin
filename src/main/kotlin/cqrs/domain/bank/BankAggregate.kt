package cqrs.domain.bank

import arrow.core.Either
import arrow.core.Option
import cqrs.domain.account.AccountAggregate
import cqrs.domain.common.Aggregate
import cqrs.domain.common.EventStore
import java.util.*

data class BankAggregate(override val aggregateId: UUID,
                         override val eventStore: EventStore,
                         private val accounts: Map<String, UUID> = mapOf())
    : Aggregate<BankAggregate, BankEvent, BankCommand> {
    override val aggregateType: String = "account"

    fun retrieveAccount(accountId: UUID): Option<AccountAggregate> {
        return if (accounts.values.contains(accountId)) {
            Option.just(AccountAggregate.loadAccount(accountId, eventStore))
        } else {
            Option.empty()
        }
    }

    fun retrieveAccountBySSN(ssn: String) =
            Option.fromNullable(accounts[ssn]).map {
                AccountAggregate.loadAccount(it, eventStore)
            }

    private fun alreadyExists(createAccount: CreateAccount) =
            accounts.values.contains(createAccount.accountId) || accounts.keys.contains(createAccount.ssn)

    override fun commandToEvents(command: BankCommand): Either<Exception, List<BankEvent>> {
        return try {
            return when (command) {
                is CreateAccount -> if (alreadyExists(command)) {
                    Either.left(AccountAlreadyExisting(command))
                } else {
                    Either.right(listOf(AccountCreated(command.bankId, command.ssn, command.accountId)))
                }
            }
        } catch (e: Exception) {
            Either.left(e)
        }
    }

    override fun apply(event: BankEvent): BankAggregate =
            when (event) {
                is AccountCreated -> this.copy(accounts = accounts + (event.ssn to event.accountId))
            }

    override fun toString(): String {
        return "BankAggregate(bankId=$aggregateId,accounts=$accounts)"
    }

}
