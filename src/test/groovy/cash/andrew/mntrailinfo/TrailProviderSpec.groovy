package cash.andrew.mntrailinfo

import cash.andrew.mntrailinfo.model.TrailInfo
import cash.andrew.mntrailinfo.model.TrailRegion
import groovy.xml.MarkupBuilder
import ratpack.exec.Promise
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.Month

class TrailProviderSpec extends Specification {

  @AutoCleanup ExecHarness execHarness = ExecHarness.harness()

  void "should parse website into a list of trail regions"() {
    given:
    TrailWebsiteProvider websiteProvider = Mock()
    def parser = new TrailWebsiteDateParser()
    def trailProvider = new TrailProvider(websiteProvider, parser)

    def trailInfo = new TrailInfo(
            name: 'Mr. Meeseeks',
            status: 'Can do!',
            description: 'Look at me!',
            lastUpdated: LocalDateTime.of(2016, Month.APRIL, 5, 16, 16)
    )
    def trailRegion = new TrailRegion(name: 'Meeseeks and destroy', trails: [trailInfo])

    def writer = new StringWriter()
    def htmlBuilder = new MarkupBuilder(writer)
    htmlBuilder.html {
      table {
        tr(class: 'conditionhead') {
          td {
            p('Nothing to see here') // not used
            span(trailRegion.name)
          }
        }
      }
      table(class: 'forumbits') {
        tr { } // empty where headers would be
        tr {
          td(trailInfo.name)
          td(trailInfo.status)
          td('Nothing to see here') // not used
          td(trailInfo.description)
          td('Nothing to see here') // not used
          td(LocalDateTime.parse(trailInfo.lastUpdated).format(parser.formatter))
        }
      }
    }

    when:
    List<TrailRegion> regions = execHarness.yield { trailProvider.provideTrails() }.value

    then:
    regions == [trailRegion]
    1 * websiteProvider.website >> Promise.value(writer.toString())
  }
}
