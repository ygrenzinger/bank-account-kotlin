package cqrs.domain.common

interface EventBus {
    fun attach(view: View<*, Event>)
    fun sendEvent(event: Event)
}