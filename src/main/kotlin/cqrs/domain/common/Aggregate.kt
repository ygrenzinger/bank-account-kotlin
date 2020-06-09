package cqrs.domain.common

import arrow.core.Either
import arrow.core.extensions.list.foldable.foldLeft
import arrow.core.flatMap
import java.util.*

@Suppress("UNCHECKED_CAST")
abstract class Aggregate<T : Aggregate<T, E, C>, E : Event, C : Command>(
        val aggregateId: UUID,
        protected val aggregateType: String,
        protected val eventStore: EventStore) {

    abstract fun applyEvent(event: E): T

    abstract fun commandToEvents(command: C): Either<Exception, List<E>>

    fun processCommand(command: C): Either<Exception, T> {
        return try {
            commandToEvents(command).map { events ->
                events.forEach { eventStore.storeEvent(aggregateType, it) }
                events.fold(this as T) { a, e -> a.applyEvent(e) }
            }
        } catch (e: Exception) {
            Either.left(e)
        }
    }

    fun processCommands(vararg commands: C): Either<Exception, T> {
        val first: Either<Exception, T> = processCommand(commands.first())
        return commands.drop(1).foldLeft(first) { acc: Either<Exception, T>, c: C ->
            acc.flatMap { it.processCommand(c) }
        }
    }

    fun rehydrate() {
        eventStore.retrieveEvents(aggregateType, aggregateId).forEach {
            applyEvent(it as E)
        }
    }
}