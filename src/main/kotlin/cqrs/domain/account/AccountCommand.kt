package cqrs.domain.account

import cqrs.domain.common.Command
import cqrs.domain.common.Event
import cqrs.domain.common.Money
import java.time.LocalDate
import java.util.*

sealed class AccountCommand(private val accountId: UUID) : Command {
    override fun aggregateIdentifier() = accountId
}

data class MakeDeposit(val accountId: UUID, val amount: Money, val date: LocalDate) : AccountCommand(accountId)
data class MakeWithdraw(val accountId: UUID, val amount: Money, val date: LocalDate) : AccountCommand(accountId)