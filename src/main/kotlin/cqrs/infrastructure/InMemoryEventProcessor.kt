package cqrs.infrastructure

import cqrs.domain.common.Aggregate
import cqrs.domain.common.Event
import cqrs.domain.common.EventProcessor
import cqrs.domain.common.View
import java.util.*

class InMemoryEventProcessor() : EventProcessor {

    data class Row(val aggregateType: String, val aggregateId: UUID, val event: Event)

    private val db = mutableListOf<Row>()
    private val listeners = mutableMapOf<String, List<View<*, Event>>>()

    override fun pushEvent(aggregateType: String, event: Event) {
        db.add(Row(aggregateType, event.aggregateIdentifier(), event))
        sendEvent(event)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : Event> retrieveEvents(aggregate: Aggregate<*, E, *>): List<E> {
        return db.filter {
            it.aggregateType == aggregate.aggregateType && it.aggregateId == aggregate.aggregateId
        }.map {
            it.event
        } as List<E>
    }

    override fun attach(view: View<*, Event>) {
        listeners.compute(view.selecting()) { _, views ->
            if (views == null) {
                listOf(view)
            } else {
                views + view
            }
        }
    }

    override fun sendEvent(event: Event) {
        listeners[event.type()]?.forEach {
            it.apply(event)
        }
    }
}
