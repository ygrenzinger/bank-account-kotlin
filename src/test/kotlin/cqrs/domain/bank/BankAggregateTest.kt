package cqrs.domain.bank

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.orNull
import cqrs.domain.bank.BankAggregate.Companion.loadBank
import cqrs.domain.common.EventStore
import cqrs.infrastructure.InMemoryEventProcessor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
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
        eventStore = InMemoryEventProcessor()
        bankAggregate = BankAggregate(bankId, eventStore)
        bankAggregate.process(CreateAccount(bankId, accountId, "ssn"))
    }

    "should have create a new account" {
        eventStore.retrieveEvents(bankAggregate) shouldContainExactly listOf(AccountCreated(bankId, "ssn", accountId))
    }

    "should retrieve account" {
        val foundAccount = loadBank(bankId, eventStore)?.retrieveAccount(accountId)!!
        foundAccount.aggregateId shouldBe accountId
    }

    "should retrieve account by SSN" {
        val foundAccount = loadBank(bankId, eventStore)?.retrieveAccountBySSN("ssn")!!
        foundAccount.aggregateId shouldBe accountId
    }

    "should fail if accountId already exist" {
        val updated = bankAggregate.process(CreateAccount(bankId, accountId, "ssn1"))
        val createNewAccount = CreateAccount(bankId, accountId, "ssn2")
        val result = updated.flatMap { it.process(createNewAccount) }
        result shouldBe Either.left(AccountAlreadyExisting(createNewAccount))
    }

    "should fail if social security number is already associated to another account" {
        val updated = bankAggregate.process(CreateAccount(bankId, accountId, "ssn1"))
        val createNewAccount = CreateAccount(bankId, UUID.randomUUID(), "ssn1")
        val result = updated.flatMap { it.process(createNewAccount) }
        result shouldBe Either.left(AccountAlreadyExisting(createNewAccount))
    }
})