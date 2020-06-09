package cqrs.domain.bank

import cqrs.domain.common.Event
import java.util.*

sealed class BankEvent : Event

data class AccountCreated(val bankId: UUID, val accountId: UUID) : BankEvent() {
    override fun aggregateIdentifier() = bankId
}