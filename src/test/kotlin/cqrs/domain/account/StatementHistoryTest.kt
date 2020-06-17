package cqrs.domain.account

import cqrs.domain.account.view.StatementHistory
import cqrs.domain.bank.MoneyTransfer
import cqrs.domain.common.EventProcessor
import cqrs.domain.common.Money
import cqrs.domain.common.createView
import cqrs.infrastructure.InMemoryEventProcessor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.util.*

class StatementHistoryTest : StringSpec({

    lateinit var accountAggregate: AccountAggregate
    lateinit var otherAccount: AccountAggregate
    lateinit var eventProcessor: EventProcessor
    lateinit var accountUUID: UUID
    lateinit var statementHistory: StatementHistory

    beforeTest {
        accountUUID = UUID.randomUUID()
        eventProcessor = InMemoryEventProcessor()
        accountAggregate = AccountAggregate(accountUUID, eventProcessor)
        statementHistory = eventProcessor.createView { StatementHistory(accountAggregate) }
        otherAccount = AccountAggregate(UUID.randomUUID(), eventProcessor)
    }

    "should build statement history" {
        val transferId = UUID.randomUUID()
        accountAggregate.process(
                MakeDeposit(accountUUID, Money.of(100.0), LocalDate.of(2020, 1, 12)),
                MakeWithdraw(accountUUID, Money.of(30.0), LocalDate.of(2020, 1, 12)),
                MakeDeposit(accountUUID, Money.of(130.0), LocalDate.of(2020, 1, 13))
        ).map {
            MoneyTransfer.transferMoney(it, otherAccount, Money.of(100.0), LocalDate.of(2020, 1, 14), transferId)
        }
        val expected = """
|    date    | amount | balance | label |
| 2020-01-14 | -100.00 | 100.00 | Transfer $transferId |
| 2020-01-13 | 130.00 | 200.00 |  |
| 2020-01-12 | -30.00 | 70.00 |  |
| 2020-01-12 | 100.00 | 100.00 |  |
        """.trimIndent()
        statementHistory.lines().joinToString("\n") { it } shouldBe expected
    }
})
