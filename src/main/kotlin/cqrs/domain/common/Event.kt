package cqrs.domain.common

import java.util.*

interface Event {
    fun aggregateIdentifier(): UUID
}