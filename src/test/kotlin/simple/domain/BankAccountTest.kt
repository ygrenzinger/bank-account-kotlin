package simple.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class BankAccountTest : StringSpec({

    "should allow deposit money to bank account" {
        BankAccount.create(BigDecimal(100)).deposit(BigDecimal(25)).balance shouldBe BigDecimal(125)
    }

    "should allow withdraw money from bank account" {
        BankAccount.create(BigDecimal(100)).withdraw(BigDecimal(25)).balance shouldBe BigDecimal(75)
    }

    "should allow transfer of money between two bank accounts" {
        val bankAccountA = BankAccount.create(BigDecimal(100))
        val bankAccountB = BankAccount.create(BigDecimal(100))
        bankAccountA.transferTo(BigDecimal(25), bankAccountB)

        bankAccountA.balance shouldBe BigDecimal(75)
        bankAccountB.balance shouldBe BigDecimal(125)
    }

})
