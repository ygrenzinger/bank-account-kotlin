package cqrs.domain.common

import arrow.core.extensions.list.foldable.foldLeft

interface EventStore {
    fun pushEvent(aggregateType: String, event: Event)
    fun <E : Event> retrieveEvents(aggregate: Aggregate<*,E,*>): List<E>

    fun <A : Aggregate<A, E, *>, E : Event> rehydrate(create: () -> A): A? {
        val aggregate = create()
        val events = this.retrieveEvents(aggregate)
        return if (events.isEmpty()) {
            null
        } else {
            events
                    .foldLeft(create()) { a, e ->
                        a.apply(e)
                    }
        }
    }
}
