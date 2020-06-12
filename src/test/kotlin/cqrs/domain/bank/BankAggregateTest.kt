package cqrs.domain.bank

import arrow.core.Either
import cqrs.domain.common.EventStore
import cqrs.infrastructure.InMemoryEventProcessor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*

class BankAggregateTest : StringSpec({
    lateinit var bankAggregate: BankAggregate
    lateinit var eventStore: EventStore
    lateinit var bankId: UUID
    lateinit var accountId: UUID

    beforeTest {
        bankId = UUID.randomUUID()
        accountId = UUID.randomUUID()
        eventStore = InMemoryEventProcessor
        bankAggregate = BankAggregate(bankId, eventStore)
    }

    "should create a new account" {
        bankAggregate.decideFor(CreateAccount(bankId, accountId, "ssn"))
        val foundAccount = bankAggregate.retrieveAccount(accountId).orNull()!!
        foundAccount.aggregateId shouldBe accountId
    }

    "should retrieve account by SSN" {
        bankAggregate.decideFor(CreateAccount(bankId, accountId, "ssn"))
        val foundAccount = bankAggregate.retrieveAccountBySSN("ssn").orNull()!!
        foundAccount.aggregateId shouldBe accountId
    }

    "should fail if accountId already exist" {
        bankAggregate.decideFor(CreateAccount(bankId, accountId, "ssn1"))
        val createNewAccount = CreateAccount(bankId, accountId, "ssn2")
        val result = bankAggregate.decideFor(createNewAccount)
        result shouldBe Either.left(AccountAlreadyExisting(createNewAccount))
    }

    "should fail if social security number is already associated to another account" {
        bankAggregate.decideFor(CreateAccount(bankId, accountId, "ssn1"))
        val createNewAccount = CreateAccount(bankId, UUID.randomUUID(), "ssn1")
        val result = bankAggregate.decideFor(createNewAccount)
        result shouldBe Either.left(AccountAlreadyExisting(createNewAccount))
    }
})