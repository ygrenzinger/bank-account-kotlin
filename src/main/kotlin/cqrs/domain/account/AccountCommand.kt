package cqrs.domain.account

import cqrs.domain.common.Command
import cqrs.domain.common.Event
import cqrs.domain.common.Money
import java.util.*

sealed class AccountCommand(private val accountId: UUID) : Command {
    override fun aggregateIdentifier() = accountId
}

data class MakeDeposit(val accountId: UUID, val amount: Money) : AccountCommand(accountId)
data class MakeWithdraw(val accountId: UUID, val amount: Money) : AccountCommand(accountId)