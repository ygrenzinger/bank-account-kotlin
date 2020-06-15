package cqrs.domain.bank

import arrow.core.Either
import arrow.core.orNull
import cqrs.domain.account.AccountAggregate
import cqrs.domain.account.MakeDeposit
import cqrs.domain.account.NotEnoughMoney
import cqrs.domain.common.Event
import cqrs.domain.common.EventStore
import cqrs.domain.common.Money
import cqrs.infrastructure.InMemoryEventProcessor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.util.*

class MoneyTransferTest : StringSpec({

    lateinit var eventStore: EventStore
    lateinit var accountA: AccountAggregate
    lateinit var accountB: AccountAggregate

    beforeTest {
        eventStore = InMemoryEventProcessor()
        accountA = AccountAggregate(UUID.randomUUID(), eventStore)
        accountA = accountA.process(MakeDeposit(accountA.aggregateId, Money.of(100.0), LocalDate.now())).orNull()!!
        accountB = AccountAggregate(UUID.randomUUID(), eventStore)
    }

    "should transfer money between two account" {
        val (updateAccountA, updateAccountB) = MoneyTransfer.transferMoney(accountA, accountB, Money.of(100.0), LocalDate.now()).orNull()!!
        updateAccountA.balance shouldBe Money.zero
        updateAccountB.balance shouldBe Money.of(100.0)
    }

    "should not transfer money if withdraw account has not enough money" {
        val result = MoneyTransfer.transferMoney(accountB, accountA, Money.of(100.0), LocalDate.now())
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

            override fun retrieveEvents(aggregateType: String, aggregateId: UUID): List<Event> {
                return emptyList()
            }

        })
        val result = MoneyTransfer.transferMoney(accountA, accountB, Money.of(100.0), LocalDate.now())
        result shouldBe Either.left(TransferFailException(exception))
        accountA.balance shouldBe Money.of(100.0)
        accountB.balance shouldBe Money.zero
    }
})