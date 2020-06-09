package cqrs.domain.account

import cqrs.domain.common.Money
import cqrs.domain.common.Event
import java.util.*

sealed class AccountEvent(private val accountId: UUID) : Event {
    override fun aggregateIdentifier() = accountId
}

data class DepositMade(val accountId: UUID, val amount: Money) : AccountEvent(accountId)
data class WithdrawMade(val accountId: UUID, val amount: Money) : AccountEvent(accountId)