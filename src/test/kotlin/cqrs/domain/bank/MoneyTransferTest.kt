package cqrs.domain.bank

import arrow.core.Either
import arrow.core.orNull
import cqrs.domain.account.*
import cqrs.domain.account.AccountAggregate.Companion.loadAccount
import cqrs.domain.common.Aggregate
import cqrs.domain.common.Event
import cqrs.domain.common.EventStore
import cqrs.domain.common.Money
import cqrs.infrastructure.InMemoryEventProcessor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.util.*

class MoneyTransferTest : StringSpec({

    lateinit var eventStore: EventStore
    lateinit var accountA: AccountAggregate
    lateinit var accountB: AccountAggregate
    lateinit var transferId: UUID

    beforeTest {
        eventStore = InMemoryEventProcessor()
        accountA = AccountAggregate(UUID.randomUUID(), eventStore)
        accountA = accountA.process(MakeDeposit(accountA.aggregateId, Money.of(100.0), LocalDate.now())).orNull()!!
        accountB = AccountAggregate(UUID.randomUUID(), eventStore)
        transferId = UUID.randomUUID()
    }

    "should transfer money between two account" {
        val date = LocalDate.now()
        MoneyTransfer.transferMoney(accountA, accountB, Money.of(100.0), date, transferId)
        eventStore.retrieveEvents(accountA) shouldContainAll  listOf(TransferWithdrawMade(accountA.aggregateId, transferId, Money.of(100.0), date))
        eventStore.retrieveEvents(accountB) shouldContainAll listOf(TransferDepositMade(accountB.aggregateId, transferId, Money.of(100.0), date))
        loadAccount(accountB.aggregateId, eventStore)!!.balance shouldBe Money.of(100.0)
        loadAccount(accountA.aggregateId, eventStore)!!.balance shouldBe Money.of(0.0)
    }

    "should not transfer money if withdraw account has not enough money" {
        val result = MoneyTransfer.transferMoney(accountB, accountA, Money.of(100.0), LocalDate.now(), transferId)
        result shouldBe Either.Left(NotEnoughMoney(Money.of(100.0)))
    }

    "should cancel transfer and put back money if deposit part fail" {
        val exception = Exception("simulated error")
        accountB = AccountAggregate(UUID.randomUUID(), object : EventStore {
            override fun pushEvent(aggregateType: String, event: Event) {
                if (event.aggregateIdentifier() == accountB.aggregateId) {
                    throw exception
                }
            }

            override fun <E : Event> retrieveEvents(aggregate: Aggregate<*, E, *>): List<E> {
                return emptyList()
            }

        })
        val result = MoneyTransfer.transferMoney(accountA, accountB, Money.of(100.0), LocalDate.now(), transferId)
        result shouldBe Either.left(TransferFailException(exception))
        loadAccount(accountA.aggregateId, eventStore)!!.balance shouldBe Money.of(100.0)
    }
})