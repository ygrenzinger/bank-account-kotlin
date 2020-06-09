package cqrs.infrastructure

import cqrs.domain.common.Event
import cqrs.domain.common.EventStore
import java.util.*

class InMemoryEventStore : EventStore {

    data class Row(val aggregateType: String, val aggregateId: UUID, val event: Event)

    private val db = mutableListOf<Row>()

    override fun storeEvent(aggregateType: String, event: Event) {
        db.add(Row(aggregateType, event.aggregateIdentifier(), event))
    }

    override fun retrieveEvents(aggregateType: String, aggregateId: UUID): List<Event> {
        return db.filter {
            it.aggregateType == aggregateType && it.aggregateId == aggregateId
        }.map {
            it.event
        }
    }
}