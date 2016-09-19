package cash.andrew.mntrailinfo

import cash.andrew.mntrailinfo.model.TrailInfo
import cash.andrew.mntrailinfo.model.TrailRegion
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import ratpack.exec.Promise

import static com.google.common.base.Preconditions.checkNotNull

@Singleton
class TrailProvider {

  private final TrailWebsiteProvider websiteProvider
  private final TrailWebsiteDateParser parser

  @Inject TrailProvider(TrailWebsiteProvider websiteProvider, TrailWebsiteDateParser parser) {
    this.websiteProvider = checkNotNull(websiteProvider, 'websiteProvider == null')
    this.parser = checkNotNull(parser, 'parser == null')
  }

  Promise<List<TrailRegion>> provideTrails() {
    websiteProvider.website.map { String websiteHtml ->
      def trails = []
      def doc = Jsoup.parse(websiteHtml)
      def dataTable = doc.getElementsByClass('forumbits')

      doc.getElementsByClass('conditionhead').eachWithIndex { Element tableRow, int i ->
        def trailInfoList = []
        dataTable[i].getElementsByTag('tr')[1..-1].each { Element tr ->
          def td = tr.children()
          def name = td[0].text()
          def status = td[1].text()
          def description = td[3].text()
          def lastUpdated = td[5].text()

          def trailInfo = new TrailInfo(
                  name: name,
                  status: status,
                  description: description,
                  lastUpdated: parser.parseText(lastUpdated)
          )
          trailInfoList << trailInfo
        }

        def title = tableRow.child(0).child(1).text()
        def trailRegion = new TrailRegion(name: title, trails: trailInfoList)

        trails << trailRegion
      }

      return trails
    }
  }
}
