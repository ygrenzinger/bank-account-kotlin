package cqrs.domain.account

import cqrs.domain.common.Money
import cqrs.infrastructure.InMemoryEventStore
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*

class AccountAggregateTest : StringSpec({

    lateinit var accountAggregate: AccountAggregate
    val eventStore = InMemoryEventStore()
    lateinit var uuid : UUID

    beforeTest {
        uuid = UUID.randomUUID()
        accountAggregate = AccountAggregate(uuid, eventStore)
    }

    "should have created bank account" {
        accountAggregate.aggregateId shouldBe uuid
        accountAggregate.balance shouldBe Money.of(0.0)
    }

    "should make deposits on bank account" {
        accountAggregate.processCommand(MakeDeposit(uuid, Money.of(100.0)))
        accountAggregate.processCommand(MakeDeposit(uuid, Money.of(20.0)))
        accountAggregate.balance shouldBe Money.of(120.0)
    }

    "should make withdraws on bank account" {
        accountAggregate.processCommands(
                MakeDeposit(uuid, Money.of(100.0)),
                MakeWithdraw(uuid, Money.of(20.0)),
                MakeWithdraw(uuid, Money.of(15.0))
        )
        accountAggregate.balance shouldBe Money.of(65.0)
    }

    "should allow to rehydrate account from db" {
        accountAggregate.processCommands(
                MakeDeposit(uuid, Money.of(100.0)),
                MakeWithdraw(uuid, Money.of(20.0))
        )
        val reloaded = AccountAggregate(uuid, eventStore)
        reloaded.rehydrate()
        accountAggregate.aggregateId shouldBe reloaded.aggregateId
        accountAggregate.balance shouldBe reloaded.balance
    }

})
