package cqrs.infrastructure

import cqrs.domain.common.Event
import cqrs.domain.common.EventProcessor
import java.util.*

class InMemoryEventProcessor() : EventProcessor {

    data class Row(val aggregateType: String, val aggregateId: UUID, val event: Event)

    private val db = mutableListOf<Row>()
    private val listeners = mutableSetOf<(Event) -> Unit>()

    override fun pushEvent(aggregateType: String, event: Event) {
        db.add(Row(aggregateType, event.aggregateIdentifier(), event))
        sendEvent(event)
    }

    override fun retrieveEvents(aggregateType: String, aggregateId: UUID): List<Event> {
        return db.filter {
            it.aggregateType == aggregateType && it.aggregateId == aggregateId
        }.map {
            it.event
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun attach(consumer: (Event) -> Unit) {
        listeners.add(consumer)
    }

    override fun sendEvent(event: Event) {
        listeners.forEach {
            it(event)
        }
    }
}
