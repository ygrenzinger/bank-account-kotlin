package cqrs.domain.common

import arrow.core.Either
import arrow.core.extensions.list.foldable.foldLeft
import arrow.core.flatMap
import java.util.*

@Suppress("UNCHECKED_CAST")
interface Aggregate<A : Aggregate<A, E, C>, E : Event, C : Command> {
    val aggregateId: UUID
    val aggregateType: String
    val eventStore: EventStore

    fun apply(event: E): A

    fun commandToEvents(command: C): Either<Exception, List<E>>

    fun process(command: C): Either<Exception, A> {
        return try {
            commandToEvents(command).map { events ->
                events.forEach { eventStore.pushEvent(aggregateType, it) }
                events.fold(this as A) { aggregate, event -> aggregate.apply(event) }
            }
        } catch (e: Exception) {
            Either.left(e)
        }
    }

    fun process(vararg commands: C): Either<Exception, A> {
        return commands.drop(1)
                .foldLeft(process(commands.first())) { acc, c ->
                    acc.flatMap { it.process(c) }
                }
    }
}
