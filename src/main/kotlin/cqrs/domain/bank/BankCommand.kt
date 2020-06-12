package cqrs.domain.bank

import cqrs.domain.common.Command
import java.util.*

sealed class BankCommand(private val bankId: UUID) : Command {
    override fun aggregateIdentifier() = bankId
}

data class CreateAccount(val bankId: UUID, val accountId: UUID, val ssn: String) : BankCommand(bankId)
