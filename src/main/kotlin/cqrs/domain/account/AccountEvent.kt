package cqrs.domain.account

import cqrs.domain.common.Event
import cqrs.domain.common.Money
import java.time.LocalDate
import java.util.*

sealed class AccountEvent(private val accountId: UUID) : Event {
    override fun aggregateIdentifier() = accountId
}

data class DepositMade(val accountId: UUID, val amount: Money, val date: LocalDate) : AccountEvent(accountId)
data class WithdrawMade(val accountId: UUID, val amount: Money, val date: LocalDate) : AccountEvent(accountId)