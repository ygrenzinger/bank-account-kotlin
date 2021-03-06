# Bank Account Kata in Kotlin

There is a simple implementation in the simple package and an advanced CQRS / ES version in CQRS package.

This is the schema explaining quickly the ES/CQRS version
![ES / CQRS Architecture](EventSourcing.png)

When a user send a command, it's sent to the aggregate. The aggregate should be unique in the whole system (even distributed). To that a fully functional system it would need some command bus or router.

The command is validated by the aggregate against business rules which generate a list of "pure" events ([process function](https://github.com/ygrenzinger/bank-account-kotlin/blob/master/src/main/kotlin/cqrs/domain/common/Aggregate.kt#L17)). These events are first saved inside the event store to ensure durability and then applied directly to the aggregate ([apply function](https://github.com/ygrenzinger/bank-account-kotlin/blob/master/src/main/kotlin/cqrs/domain/common/Aggregate.kt#L13)). At the same time, the events are pushed by the event bus to the attached views.

The money transfer is implemented with specific events, business rules (can't have negative amount) and compensating events if the deposit money part fail for some reasons.

Any way to improve it would be welcomed! :)  
Especially the generics use inside [Aggregate](https://github.com/ygrenzinger/bank-account-kotlin/blob/master/src/main/kotlin/cqrs/domain/common/Aggregate.kt), [View](https://github.com/ygrenzinger/bank-account-kotlin/blob/master/src/main/kotlin/cqrs/domain/common/View.kt) or [EventProcess](https://github.com/ygrenzinger/bank-account-kotlin/blob/master/src/main/kotlin/cqrs/domain/common/EventProcessor.kt)
