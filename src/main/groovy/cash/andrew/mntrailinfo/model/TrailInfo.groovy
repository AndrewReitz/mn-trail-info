package cash.andrew.mntrailinfo.model

import groovy.transform.Immutable
import groovy.transform.ToString

import java.time.LocalDateTime

@Immutable(knownImmutableClasses = [LocalDateTime])
@ToString(includeNames = true)
class TrailInfo {
    String name
    String status
    String description
    String lastUpdated
}
