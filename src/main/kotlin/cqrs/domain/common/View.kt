package cqrs.domain.common

interface View<A: Aggregate<*,*,*>, E : Event> {
    val associatedAggregate : A
    fun apply(event: E)
    fun selecting(): String

    @Suppress("UNCHECKED_CAST")
    fun attachToBus(eventBus: EventBus) {
        eventBus.attach(this as View<*, Event>)
    }
}