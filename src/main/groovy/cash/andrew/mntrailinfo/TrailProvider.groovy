package cash.andrew.mntrailinfo

import cash.andrew.mntrailinfo.model.TrailInfo
import cash.andrew.mntrailinfo.model.TrailRegion
import com.google.inject.Inject
import com.google.inject.Singleton
import groovy.util.logging.Slf4j
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import ratpack.exec.Promise
import rx.observables.GroupedObservable

import static com.google.common.base.Preconditions.checkNotNull

@Slf4j
@Singleton
class TrailProvider {

  private final TrailWebsiteProvider websiteProvider
  private final TrailWebsiteDateParser parser

  @Inject TrailProvider(TrailWebsiteProvider websiteProvider, TrailWebsiteDateParser parser) {
    this.websiteProvider = checkNotNull(websiteProvider, 'websiteProvider == null')
    this.parser = checkNotNull(parser, 'parser == null')
  }

  Promise<List<TrailRegion>> provideTrails() {
    return websiteProvider.website.observe().map { String trailConditionsHtml ->
      return Jsoup.parse(trailConditionsHtml)
    }.map { Document doc ->
      def dataTable = doc.getElementsByClass('forumbits')

      def returnValue = []
      doc.getElementsByClass('conditionhead').eachWithIndex { Element tableRow, int i ->
        def trailRegionName = tableRow.child(0).child(1).text()
        dataTable[i].getElementsByTag('tr')[1..-1].each { Element tr ->
          returnValue << [td: tr.children(), trailRegion: trailRegionName]
        }
      }
      return returnValue
    }.flatMapIterable { data -> data }
    .flatMap { Map data ->
      Elements td = data.td
      def trailConditionThread = td[0].child(0).attr('href').split('=')[1]
      return websiteProvider.trailDetails(trailConditionThread)
              .observe()
              .map { String detailsHtml ->
                return Jsoup.parse(detailsHtml)
              }.map { Document detailsDoc ->
                return detailsDoc.getElementsByClass('content')[0].text().split('Details:')[1]
              }.map { String fullDescription ->
                def name = td[0].text()
                def status = td[1].text()
                def description = td[3].text()
                def lastUpdated = td[5].text()

                def trailInfo = new TrailInfo(
                        name: name,
                        status: status,
                        description: description,
                        fullDescription: fullDescription,
                        lastUpdated: parser.parseText(lastUpdated)
                )

                return [trailInfo: trailInfo, trailRegion: data.trailRegionName] as Map
              }
    }.groupBy { Map data -> data.trailRegionName }
    .flatMap { GroupedObservable groupedObservable ->
      return groupedObservable.map { Map item ->
        item.trailInfo
      }.toList()
      .map { List<TrailInfo> trailInfos ->
        return new TrailRegion(name: groupedObservable.key, trails: trailInfos)
      }
    }.promise()
  }
}
