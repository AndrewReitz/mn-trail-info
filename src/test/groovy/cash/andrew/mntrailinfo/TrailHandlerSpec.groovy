package cash.andrew.mntrailinfo

import cash.andrew.mntrailinfo.model.TrailInfo
import cash.andrew.mntrailinfo.model.TrailRegion
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import ratpack.exec.Promise
import ratpack.jackson.JsonRender
import ratpack.test.handling.RequestFixture
import spock.lang.Specification

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import static io.netty.handler.codec.http.HttpHeaderNames.*
import static java.lang.Integer.toHexString

class TrailHandlerSpec extends Specification {

  RequestFixture requestFixture = RequestFixture.requestFixture()

  void "should render json data"() {
    given: 'setup handler'
    TrailProvider trailProvider = Mock()
    Cache cache = Caffeine.newBuilder().build()
    def handler = new TrailHandler(cache, trailProvider)

    and: 'setup models'
    def expectedTrail = new TrailInfo(
            name: 'Plumbus',
            status: 'soft',
            description: 'Everyone has a Plumbus',
            lastUpdated: LocalDateTime.now()
    )
    def expectedRegion = new TrailRegion(
            name: 'C-137',
            trails: [expectedTrail]
    )
    def expected = [expectedRegion]

    when: 'first response from handler'
    def result = requestFixture.handle(handler)

    then:
    result.status.code == 200
    result.headers.get(CACHE_CONTROL) == "max-age=${TimeUnit.MINUTES.toSeconds(5)}" as String
    result.headers.get(ETAG) == toHexString(expected.hashCode())
    cache.asMap().size() == 1
    result.rendered(JsonRender).object == expected
    1 * trailProvider.provideTrails() >> Promise.value(expected)

    when: 'second response without etag from handler'
    result = requestFixture.handle(handler)

    then:
    result.status.code == 200
    result.headers.get(CACHE_CONTROL) == "max-age=${TimeUnit.MINUTES.toSeconds(5)}" as String
    result.headers.get(ETAG) == toHexString(expected.hashCode())
    result.rendered(JsonRender).object == expected

    when: 'second response with etag from handler'
    result = requestFixture.header(IF_NONE_MATCH, toHexString(expected.hashCode()))
            .handle(handler)

    then:
    result.status.code == 304
  }
}
