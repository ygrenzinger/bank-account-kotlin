package cqrs.domain.bank

import cqrs.domain.common.Event
import java.util.*

sealed class BankEvent(private val bankId: UUID) : Event {
    override fun aggregateIdentifier() = bankId
    override fun type() = TYPE

    companion object {
        const val TYPE = "bank-event"
    }
}

data class AccountCreated(val bankId: UUID, val ssn: String, val accountId: UUID) : BankEvent(bankId)
