package cqrs.domain.common

import java.util.UUID

interface Command {
    fun aggregateIdentifier(): UUID
}
