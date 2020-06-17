package cqrs.domain.account

import cqrs.domain.common.Event
import cqrs.domain.common.Money
import java.time.LocalDate
import java.util.*

sealed class AccountEvent(private val accountId: UUID) : Event {
    override fun aggregateIdentifier() = accountId
    override fun type() = TYPE

    companion object {
        const val TYPE = "account-event"
    }
}

data class DepositMade(val accountId: UUID, val amount: Money, val date: LocalDate) : AccountEvent(accountId)
data class WithdrawMade(val accountId: UUID, val amount: Money, val date: LocalDate) : AccountEvent(accountId)
data class TransferWithdrawMade(val accountId: UUID, val transferId: UUID, val amount: Money, val date: LocalDate) : AccountEvent(accountId)
data class TransferDepositMade(val accountId: UUID, val transferId: UUID, val amount: Money, val date: LocalDate) : AccountEvent(accountId)
data class TransferWithdrawCanceled(val accountId: UUID, val transferId: UUID, val amount: Money, val date: LocalDate) : AccountEvent(accountId)
