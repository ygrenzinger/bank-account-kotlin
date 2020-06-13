package cqrs.domain.common

import arrow.core.Either
import arrow.core.extensions.list.foldable.foldLeft
import arrow.core.flatMap
import java.util.*

abstract class Aggregate<A : Aggregate<A, E, C>, E : Event, C : Command>(
        val aggregateId: UUID,
        val aggregateType: String,
        protected val eventStore: EventStore) {

    abstract fun evolveWith(event: E): A

    abstract fun commandToEvents(command: C): Either<Exception, List<E>>

    fun decideFor(command: C): Either<Exception, Aggregate<A, E, C>> {
        return try {
            commandToEvents(command).map { events ->
                events.forEach { eventStore.pushEvent(aggregateType, it) }
                events.fold(this) { a, e -> a.evolveWith(e) }
            }
        } catch (e: Exception) {
            Either.left(e)
        }
    }

    fun decideFor(vararg commands: C): Either<Exception, Aggregate<A, E, C>> {
        return commands.drop(1)
                .foldLeft(decideFor(commands.first())) { acc, c ->
                    acc.flatMap { it.decideFor(c) }
                }
    }

    @Suppress("UNCHECKED_CAST")
    fun rehydrate(): Aggregate<A, E, C> {
        return eventStore.retrieveEvents(aggregateType, aggregateId)
                .foldLeft(this) { a, e ->
                    a.evolveWith(e as E)
                }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Aggregate<*, *, *>) return false

        if (aggregateId != other.aggregateId) return false
        if (aggregateType != other.aggregateType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = aggregateId.hashCode()
        result = 31 * result + aggregateType.hashCode()
        return result
    }

    override fun toString(): String {
        return "Aggregate(aggregateId=$aggregateId, aggregateType='$aggregateType')"
    }


}