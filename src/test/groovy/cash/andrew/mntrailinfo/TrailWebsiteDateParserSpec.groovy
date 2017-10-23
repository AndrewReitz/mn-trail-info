package cash.andrew.mntrailinfo

import spock.lang.Specification

import java.time.LocalDateTime
import java.time.Month

class TrailWebsiteDateParserSpec extends Specification {

	final TrailWebsiteDateParser trailWebsiteDateParser = new TrailWebsiteDateParser()

	void "should parse text to LocalDate"() {
		when:
		LocalDateTime date = trailWebsiteDateParser.parseText('04-05-2016, 04:16 PM')

		then:
		date == LocalDateTime.of(2016, Month.APRIL, 5, 16, 16)
	}
}
