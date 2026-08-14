# HHKUNGFU CloudStream v1.0

Root and module Gradle scripts were converted from Kotlin DSL to Groovy DSL.
This avoids the Kotlin DSL compiler path that was throwing:
`InvalidProtocolBufferException: Protocol message contained an invalid tag (zero)`.

Provider source `HHKungfuProvider.kt` is retained.
