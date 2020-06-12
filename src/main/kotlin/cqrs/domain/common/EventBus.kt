package cqrs.domain.common

interface EventBus {
    fun attach(consumer: (Event) -> Unit)
    fun sendEvent(event: Event)
}