package cqrs.domain.common

import java.util.*

interface EventStore {
    fun pushEvent(aggregateType: String, event: Event)
    fun retrieveEvents(aggregateType: String, aggregateId: UUID): List<Event>
}