package cqrs.infrastructure

import cqrs.domain.account.DepositMade
import cqrs.domain.account.WithdrawMade
import cqrs.domain.common.Money
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import java.util.*


internal class InMemoryEventStoreTest: StringSpec({

    "should store and retrieve events" {
        val eventStore = InMemoryEventStore()
        val uuid = UUID.randomUUID()
        val deposit = DepositMade(uuid, Money.of(15.0))
        val withdraw = WithdrawMade(uuid, Money.of(5.0))
        eventStore.storeEvent("account", deposit)
        eventStore.storeEvent("account", withdraw)

        eventStore.retrieveEvents("account", uuid) shouldContainAll listOf(deposit, withdraw)
    }
})