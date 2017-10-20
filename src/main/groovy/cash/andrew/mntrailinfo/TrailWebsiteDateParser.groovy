package cash.andrew.mntrailinfo

import groovy.transform.CompileStatic

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@CompileStatic
class TrailWebsiteDateParser {

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern('MM-dd-yyyy, hh:mm a')

	LocalDateTime parseText(String text) {
		return LocalDateTime.parse(text, formatter)
	}
}
