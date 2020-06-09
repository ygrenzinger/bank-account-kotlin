package cqrs.domain.bank

import java.util.*

data class ExistingAccountException(val accountId: UUID) : Exception()