package cqrs.domain.account

import arrow.core.Either
import arrow.core.orNull
import cqrs.domain.common.EventStore
import cqrs.domain.common.Money
import cqrs.infrastructure.InMemoryEventProcessor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.util.*

class AccountAggregateTest : StringSpec({

    lateinit var accountAggregate: AccountAggregate
    lateinit var eventStore: EventStore
    lateinit var uuid: UUID

    beforeTest {
        uuid = UUID.randomUUID()
        eventStore = InMemoryEventProcessor()
        accountAggregate = AccountAggregate(uuid, eventStore)
    }

    "should have created bank account" {
        accountAggregate.aggregateId shouldBe uuid
        accountAggregate.balance shouldBe Money.of(0.0)
    }

    "should make deposits on bank account" {
        val updated = accountAggregate.process(
                MakeDeposit(uuid, Money.of(100.0), LocalDate.now()),
                MakeDeposit(uuid, Money.of(20.0), LocalDate.now())
        ).orNull()!!
        updated.balance shouldBe Money.of(120.0)
    }

    "should make withdraws on bank account" {
        val updated = accountAggregate.process(
                MakeDeposit(uuid, Money.of(100.0), LocalDate.now()),
                MakeWithdraw(uuid, Money.of(20.0), LocalDate.now()),
                MakeWithdraw(uuid, Money.of(15.0), LocalDate.now())
        ).orNull()!!
        updated.balance shouldBe Money.of(65.0)
    }

    "should not allow to withdraw more than balance in the account" {
        val result = accountAggregate.process(
                MakeDeposit(uuid, Money.of(100.0), LocalDate.now()),
                MakeWithdraw(uuid, Money.of(120.0), LocalDate.now())
        )

        result shouldBe Either.Left(NotEnoughMoney(Money.of(20.0)))
    }

    "should allow to rehydrate account from db" {
        accountAggregate.process(
                MakeDeposit(uuid, Money.of(100.0), LocalDate.now()),
                MakeWithdraw(uuid, Money.of(20.0), LocalDate.now())
        )
        val reloaded = eventStore.rehydrate { AccountAggregate(uuid, eventStore) }!!
        reloaded.aggregateId shouldBe uuid
        reloaded.balance shouldBe Money.of(80.0)
    }

})
