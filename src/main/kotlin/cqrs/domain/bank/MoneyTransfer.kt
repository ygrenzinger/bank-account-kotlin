package cqrs.domain.bank

import arrow.core.Either
import arrow.core.extensions.fx
import cqrs.domain.account.*
import cqrs.domain.common.Money
import java.time.LocalDate
import java.util.*

data class TransferSuccessResult(val fromAccount: AccountAggregate, val toAccount: AccountAggregate)

object MoneyTransfer {
    fun transferMoney(fromAccount: AccountAggregate, toAccount: AccountAggregate, money: Money, date: LocalDate): Either<Exception, TransferSuccessResult> {
        val transferId = UUID.randomUUID()
        val transferOperation = Either.fx<Exception, TransferSuccessResult> {
            val (updatedFrom) = fromAccount.process(MakeTransferWithdraw(fromAccount.aggregateId, transferId, money, date))
            val (updatedTo) = toAccount.process(MakeTransferDeposit(toAccount.aggregateId, transferId, money, date))
            TransferSuccessResult(updatedFrom, updatedTo)
        }
        return manageDepositFailureIfAny(transferOperation, fromAccount, transferId, money)
    }

    private fun manageDepositFailureIfAny(transferSuccessOperation: Either<Exception, TransferSuccessResult>, fromAccount: AccountAggregate, transferId: UUID, money: Money): Either<java.lang.Exception, TransferSuccessResult> {
        return transferSuccessOperation.mapLeft {
            if (it !is NotEnoughMoney) {
                fromAccount.process(CancelTransferWithdraw(fromAccount.aggregateId, transferId, money))
                TransferFailException(it)
            } else {
                it
            }
        }
    }
}
