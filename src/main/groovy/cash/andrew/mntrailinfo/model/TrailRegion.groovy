package cash.andrew.mntrailinfo.model

import groovy.transform.Immutable
import groovy.transform.ToString

@Immutable
@ToString(includeNames = true)
class TrailRegion {
  String name
  List<TrailInfo> trails
}
