package cash.andrew.mntrailinfo.model

import groovy.transform.Immutable
import groovy.transform.ToString
import okhttp3.HttpUrl
import ratpack.api.Nullable

import java.time.LocalDateTime

@Immutable(knownImmutableClasses = [LocalDateTime])
@ToString(includeNames = true)
class TrailInfo {
    String name
    String status
    String description
    String lastUpdated

    @Nullable HttpUrl facebookUrl
    HttpUrl detailsUrl
}
