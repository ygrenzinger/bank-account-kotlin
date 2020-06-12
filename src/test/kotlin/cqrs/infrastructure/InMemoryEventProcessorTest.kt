package cqrs.infrastructure

import cqrs.domain.account.DepositMade
import cqrs.domain.account.WithdrawMade
import cqrs.domain.common.Money
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import java.time.LocalDate
import java.util.*


internal class InMemoryEventProcessorTest : StringSpec({

    "should store and retrieve events" {
        val eventStore = InMemoryEventProcessor()
        val uuid = UUID.randomUUID()
        val deposit = DepositMade(uuid, Money.of(15.0), LocalDate.now())
        val withdraw = WithdrawMade(uuid, Money.of(5.0), LocalDate.now())
        eventStore.pushEvent("account", deposit)
        eventStore.pushEvent("account", withdraw)

        eventStore.retrieveEvents("account", uuid) shouldContainAll listOf(deposit, withdraw)
    }
})