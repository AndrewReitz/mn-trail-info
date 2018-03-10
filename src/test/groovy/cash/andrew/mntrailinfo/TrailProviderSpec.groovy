package cash.andrew.mntrailinfo

import cash.andrew.mntrailinfo.model.TrailInfo
import cash.andrew.mntrailinfo.model.TrailRegion
import groovy.xml.MarkupBuilder
import ratpack.exec.Promise
import ratpack.http.client.HttpClient
import ratpack.http.client.ReceivedResponse
import ratpack.http.client.internal.DefaultReceivedResponse
import ratpack.http.internal.ByteBufBackedTypedData
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.lang.Void as Should
import java.time.LocalDateTime
import java.time.Month

class TrailProviderSpec extends Specification {

  @AutoCleanup ExecHarness execHarness = ExecHarness.harness()

  Should "parse website into a list of trail regions (v1)"() {
    given:
    TrailWebsiteProvider websiteProvider = Mock()
    def parser = new TrailWebsiteDateParser()
    def trailProvider = new TrailProvider(websiteProvider, parser, Mock(HttpClient))

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

  Should "parse website into a list of trails (for v2)"() {
    given:
    TrailWebsiteProvider websiteProvider = Mock()
    def parser = new TrailWebsiteDateParser()
    def trailProvider = new TrailProvider(websiteProvider, parser, Mock(HttpClient))

    def trailInfo = new TrailInfo(
        name: 'Mr. Meeseeks',
        status: 'Can do!',
        description: 'Look at me!',
        fullDescription: 'Trail is mostly tacky with a few damp spots. Should be in great shape by this evening and through the rest of the week.',
        lastUpdated: LocalDateTime.of(2016, Month.APRIL, 5, 16, 16)
        )
    def expected = [trailInfo]
    def expectedThreadId = '9180'

    def mainPageWriter = new StringWriter()
    def mainPageBuilder = new MarkupBuilder(mainPageWriter)
    mainPageBuilder.html {
      table {
        tr(class: 'conditionhead') {
          td {
            p('Nothing to see here') // not used
            span('Nothing to see here') // not used
          }
        }
      }
      table(class: 'forumbits') {
        tr { } // empty where headers would be
        tr {
          td {
            a(trailInfo.name, href: "showthread.php?t=$expectedThreadId")
          }
          td(trailInfo.status)
          td('Nothing to see here') // not used
          td(trailInfo.description)
          td('Nothing to see here') // not used
          td(LocalDateTime.parse(trailInfo.lastUpdated).format(parser.formatter))
        }
      }
    }

    def contentValue =
        '''
Trail Condition: Tacky
Date Posted: 10-16-2017 @ 12:01 PM

Details: Trail is mostly tacky with a few damp spots. Should be in great shape by this evening and through the rest of the week.
'''
    def threadPageWriter = new StringWriter()
    def threadPageBuilder = new MarkupBuilder(threadPageWriter)
    threadPageBuilder.html {
      div(contentValue, class: 'content')
    }

    when:
    List<TrailInfo> trails = execHarness.yield { trailProvider.provideTrailsV2() }.value

    then:
    trails == expected
    1 * websiteProvider.website >> Promise.value(mainPageWriter.toString())
    1 * websiteProvider.trailDetails(expectedThreadId) >> Promise.value(threadPageWriter.toString())
  }

  Should "proxy v3 request to trail api"() {
    given:
    def typedData = Mock(ByteBufBackedTypedData) {
      getText() >> "HELLO WORLD"
    }
    def response = new DefaultReceivedResponse(null, null, typedData)

    def httpClient = Mock(HttpClient)
    def classUnderTest = new TrailProvider(Mock(TrailWebsiteProvider), new TrailWebsiteDateParser(), httpClient)

    when:
    ReceivedResponse result  = execHarness.yield { classUnderTest.provideTrailsV3() }.value


    then:
    result.body.text == "HELLO WORLD"
    1 * httpClient.get(TrailProvider.V3_API_ENDPOINT) >> Promise.value(response)
  }
}
