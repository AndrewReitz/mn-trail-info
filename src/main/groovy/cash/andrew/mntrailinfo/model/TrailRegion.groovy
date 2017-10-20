package cash.andrew.mntrailinfo.model

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString

@Immutable
@ToString(includeNames = true)
@CompileStatic
class TrailRegion {
	String name
	List<TrailInfo> trails
}
