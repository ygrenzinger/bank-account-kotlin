package cqrs.domain.bank

import java.lang.Exception

data class TransferFailException(val exception: Exception) : Exception(exception)