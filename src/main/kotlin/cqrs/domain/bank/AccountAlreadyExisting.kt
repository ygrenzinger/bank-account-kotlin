package cqrs.domain.bank

data class AccountAlreadyExisting(val command: CreateAccount) : Exception()
