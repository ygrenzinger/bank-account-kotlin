package cqrs.domain.account.view

import cqrs.domain.common.Money
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate

data class StatementLine(val date: LocalDate, val value: Money, val balance: Money) {
    private fun BigDecimal.format() = df.format(this)

    fun toPrint() = "$date | ${value.amount.format()} | ${balance.amount.format()}"

    companion object {
        private fun buildDecimalFormat(): DecimalFormat {
            val decimalFormatSymbols = DecimalFormatSymbols()
            decimalFormatSymbols.decimalSeparator = '.'
            decimalFormatSymbols.groupingSeparator = ' '
            return DecimalFormat("#,##0.00", decimalFormatSymbols)
        }

        val df = buildDecimalFormat()
    }
}