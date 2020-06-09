package cqrs.domain.bank

import arrow.core.orNull
import cqrs.infrastructure.InMemoryEventStore
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import java.util.*

class BankAggregateTest : StringSpec({
    lateinit var bankAggregate: BankAggregate
    val eventStore = InMemoryEventStore()
    lateinit var bankId : UUID
    lateinit var accountId : UUID

    beforeTest {
        bankId = UUID.randomUUID()
        accountId = UUID.randomUUID()
        bankAggregate = BankAggregate(bankId, eventStore)
    }

    "should create a new account" {
        bankAggregate.processCommand(CreateAccount(bankId, accountId))
        val foundAccount = bankAggregate.retrieveAccount(accountId).orNull()!!
        foundAccount.aggregateId shouldBe accountId
    }

    "should fail if accountId already exist" {
        bankAggregate.processCommand(CreateAccount(bankId, accountId))
        val result = bankAggregate.processCommand(CreateAccount(bankId, accountId))
        result.isLeft().shouldBeTrue()
        result.swap().orNull() shouldBe ExistingAccountException(accountId)
    }


})