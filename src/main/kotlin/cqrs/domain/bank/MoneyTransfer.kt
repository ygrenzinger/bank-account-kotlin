package cqrs.domain.bank

import arrow.core.Either
import arrow.core.extensions.fx
import cqrs.domain.account.*
import cqrs.domain.common.Money
import java.time.LocalDate
import java.util.*

data class TransferSuccessResult(val fromAccount: AccountAggregate, val toAccount: AccountAggregate)

data class MoneyTransfer(val fromAccount: AccountAggregate, val toAccount: AccountAggregate, val money: Money, val date: LocalDate, val transferId: UUID) {
    fun transfer(): Either<Exception, TransferSuccessResult> {
        val transferOperation = Either.fx<Exception, TransferSuccessResult> {
            val (updatedFrom) = fromAccount.process(MakeTransferWithdraw(fromAccount.aggregateId, transferId, money, date))
            val (updatedTo) = toAccount.process(MakeTransferDeposit(toAccount.aggregateId, transferId, money, date))
            TransferSuccessResult(updatedFrom, updatedTo)
        }
        return manageDepositFailureIfAny(transferOperation)
    }

    private fun manageDepositFailureIfAny(transferSuccessOperation: Either<Exception, TransferSuccessResult>): Either<java.lang.Exception, TransferSuccessResult> {
        return transferSuccessOperation.mapLeft {
            if (it !is NotEnoughMoney) {
                fromAccount.process(CancelTransferWithdraw(fromAccount.aggregateId, transferId, money, date))
                TransferFailException(it)
            } else {
                it
            }
        }
    }

    companion object {
        fun transferMoney(fromAccount: AccountAggregate, toAccount: AccountAggregate, money: Money, date: LocalDate, transferId: UUID) =
                MoneyTransfer(fromAccount, toAccount, money, date, transferId).transfer()
    }
}
