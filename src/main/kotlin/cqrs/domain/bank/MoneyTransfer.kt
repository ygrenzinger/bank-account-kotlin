package cqrs.domain.bank

import arrow.core.Either
import arrow.core.flatMap
import cqrs.domain.account.*
import cqrs.domain.common.Aggregate
import cqrs.domain.common.Money
import java.time.LocalDate
import java.util.*

object MoneyTransfer {
    fun transferMoney(fromAccount: AccountAggregate, toAccount: AccountAggregate, money: Money, date: LocalDate): Either<Exception, Pair<Aggregate<*, *, *>, Aggregate<*, *, *>>> {
        val transferId = UUID.randomUUID()
        return fromAccount
                .process(MakeTransferWithdraw(fromAccount.aggregateId, transferId, money, date))
                .flatMap { updateFrom ->
                    toAccount.process(MakeTransferDeposit(toAccount.aggregateId, transferId, money, date))
                            .map { Pair(updateFrom, it) }
                }.mapLeft {
                    if (it !is NotEnoughMoney) {
                        fromAccount.process(CancelTransferWithdraw(fromAccount.aggregateId, transferId, money))
                        TransferFailException(it)
                    } else {
                        it
                    }
                }
    }
}
