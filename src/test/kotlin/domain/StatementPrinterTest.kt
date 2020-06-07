package domain

import domain.StatementLineFilter.Companion.onlyDeposit
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.collections.shouldContainExactly
import java.math.BigDecimal
import java.time.LocalDate

object PrinterListener : TestListener {
    val lines: MutableList<String> = mutableListOf()

    val fakePrinter = object : Printer {
        override fun printLine(line: String) {
            lines.add(line)
        }
    }

    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        lines.clear()
    }
}

class StatementPrinterTest : StringSpec({

    val account = BankAccount.create(BigDecimal(100), date = LocalDate.of(2019, 9, 1))
            .withdraw(BigDecimal(25), date = LocalDate.of(2019, 9, 2))
            .deposit(BigDecimal(40), date = LocalDate.of(2019, 9, 3))

    "should print statement" {
        StatementPrinter(PrinterListener.fakePrinter).printStatementFor(account)

        PrinterListener.lines.shouldContainExactly(
                "2019-09-03 40.00 115.00",
                "2019-09-02 -25.00 75.00",
                "2019-09-01 100.00 100.00"
        )
    }

    "should print statement with only Deposit" {
        StatementPrinter(PrinterListener.fakePrinter).printStatementFor(account, onlyDeposit)

        PrinterListener.lines.shouldContainExactly(
                "2019-09-03 40.00 115.00",
                "2019-09-01 100.00 100.00"
        )
    }

    "should print statement with only Withdraw and between specific dates" {

        val predicate = StatementLineFilter.combine(listOf(
                StatementLineFilter.onlyWithdrawal,
                StatementLineFilter.betweenDates(LocalDate.of(2019, 9, 1), LocalDate.of(2019, 9, 3))
        ))
        StatementPrinter(PrinterListener.fakePrinter).printStatementFor(account, predicate)

        PrinterListener.lines.shouldContainExactly(
                "2019-09-02 -25.00 75.00"
        )
    }
}) {
    override fun listeners(): List<TestListener> = listOf(PrinterListener)
}
