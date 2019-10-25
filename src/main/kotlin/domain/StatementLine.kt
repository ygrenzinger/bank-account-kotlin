package domain

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

data class StatementLine(val operation: Operation, val balance: BigDecimal) {
    private val df: DecimalFormat

    init {
        val decimalFormatSymbols = DecimalFormatSymbols()
        decimalFormatSymbols.decimalSeparator = '.'
        decimalFormatSymbols.groupingSeparator = ' '
        df = DecimalFormat("#,###.00", decimalFormatSymbols)
    }

    private fun BigDecimal.format() = df.format(this)

    fun toPrint() = "${operation.date} ${operation.value().format()} ${balance.format()}"
}
