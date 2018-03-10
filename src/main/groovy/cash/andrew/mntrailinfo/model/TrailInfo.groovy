package cash.andrew.mntrailinfo.model

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString

@Immutable
@ToString(includeNames = true)
@CompileStatic
class TrailInfo {
	String name
	String status
	/** Short description about 100 characters. Not changing the name to keep api versioning simpler */
	String description
	String fullDescription
	String lastUpdated
}
