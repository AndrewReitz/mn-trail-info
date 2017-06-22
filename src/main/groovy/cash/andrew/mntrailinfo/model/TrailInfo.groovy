package cash.andrew.mntrailinfo.model

import groovy.transform.Immutable
import groovy.transform.ToString

import java.time.LocalDateTime

@Immutable(knownImmutableClasses = [LocalDateTime])
@ToString(includeNames = true)
class TrailInfo {
    String name
    String status
    /** Short description about 100 characters. Not changing the name to keep api versioning simpler */
    String description
    String fullDescription
    String lastUpdated
}
