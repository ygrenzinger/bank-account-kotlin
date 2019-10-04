package domain

import java.time.LocalDate
import java.util.function.Predicate

class StatementLineFilters {
    companion object {
        fun betweenDates(startDate: LocalDate, endDate: LocalDate): Predicate<StatementLine> {
            return Predicate {
                val date = it.operation.date
                date.isEqual(startDate) || date.isEqual(endDate) || date.isAfter(startDate) && date.isBefore(endDate)
            }
        }

        val onlyDeposit: Predicate<StatementLine> = Predicate {
            it.operation is Deposit
        }
        val onlyWithdrawal: Predicate<StatementLine> = Predicate {
            it.operation is Withdrawal
        }

        fun withFilters(filters: List<Predicate<StatementLine>>): Predicate<StatementLine> {
            return filters.reduce { sum, element ->
                sum.and(element)
            }
        }
    }
}

interface Printer {
    fun printLine(line: String)
}

class StatementPrinter(private val printer: Printer) {
    fun printStatementFor(account: BankAccount, predicate: Predicate<StatementLine> = Predicate { true }) {
        account.retrieveStatementLines()
                .filter {
                    predicate.test(it)
                }
                .reversed()
                .forEach {
                    printer.printLine(it.toPrint())
                }
    }
}