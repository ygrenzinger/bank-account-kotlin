package cqrs.domain.common

interface EventProcessor : EventBus, EventStore

inline fun <reified A : Aggregate<A, E, C>, reified C : Command, reified E : Event, reified V : View<A, E>>
        EventProcessor.createView(init: () -> V): V {
    val view = init()
    this.retrieveEvents(view.associatedAggregate).forEach {
        view.apply(it)
    }
    view.attachToBus(this)
    return view
}