package domain

import arrow.core.extensions.list.foldable.combineAll
import arrow.typeclasses.Monoid
import java.time.LocalDate
import java.time.Period
import java.util.function.Predicate


class StatementLineFilter(val predicate: (StatementLine) -> Boolean) :
        Monoid<StatementLineFilter>, Predicate<StatementLine> {
    override fun test(line: StatementLine): Boolean {
        return predicate.invoke(line)
    }

    override fun empty() = StatementLineFilter.empty()

    override fun StatementLineFilter.combine(b: StatementLineFilter): StatementLineFilter {
        return StatementLineFilter {line -> this.predicate.invoke(line) && b.predicate.invoke(line) }
    }

    companion object {
        fun empty() = StatementLineFilter { true }

        fun betweenDates(startDate: LocalDate, endDate: LocalDate) = StatementLineFilter {
            val date = it.operation.date
            startDate.compareTo(date) * date.compareTo(endDate) >= 0
        }

        val onlyDeposit = StatementLineFilter {  it.operation is Deposit }
        val onlyWithdrawal = StatementLineFilter { it.operation is Withdrawal }

        fun combine(filters: List<StatementLineFilter>) = filters.combineAll(empty())
    }
}

interface Printer {
    fun printLine(line: String)
}

class StatementPrinter(private val printer: Printer) {
    fun printStatementFor(account: BankAccount, filter: StatementLineFilter = StatementLineFilter.empty()) {
        account.retrieveStatementLines()
                .filter {
                    filter.test(it)
                }
                .reversed()
                .forEach {
                    printer.printLine(it.toPrint())
                }
    }
}
