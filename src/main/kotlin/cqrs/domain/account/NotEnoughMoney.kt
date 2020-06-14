package cqrs.domain.account

import cqrs.domain.common.Money

data class NotEnoughMoney(val missingAmount: Money) : Exception()
