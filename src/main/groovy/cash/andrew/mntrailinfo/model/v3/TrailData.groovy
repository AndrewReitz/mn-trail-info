package cash.andrew.mntrailinfo.model.v3

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString

import java.time.Instant

@Immutable(knownImmutableClasses = [Instant])
@ToString(includeNames = true)
@CompileStatic
class TrailData {
  String city
  String trailName
  String zipcode
  String trailStatus
  String trailId
  Instant updatedAt
  BigDecimal longitude
  Instant createdAt
  String description
  BigDecimal latitude
  String state
  String street
}
