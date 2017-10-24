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

import java.lang.Void as Should

import static io.netty.handler.codec.http.HttpHeaderNames.*
import static java.lang.Integer.toHexString

class TrailHandlerSpec extends Specification {

  RequestFixture requestFixture = RequestFixture.requestFixture()

  Should "should render json data when no header is provided"() {
    given: 'setup handler'
    TrailProvider trailProvider = Mock()
    Cache cache = Caffeine.newBuilder().build()
    def classUnderTest = new TrailHandler(new TrailCache(cache), trailProvider)

    and: 'setup models'
    def expectedTrail = new TrailInfo(
        name: 'Plumbus',
        status: 'soft',
        description: 'Everyone has a Plumbus',
        lastUpdated: LocalDateTime.now()
        )
    def expectedRegion = new TrailRegion(
        name: 'C-137',
        trails: [expectedTrail])
    def expected = [expectedRegion]

    when: 'first response from handler'
    def result = requestFixture.handle(classUnderTest)

    then:
    result.status.code == 200
    result.headers.get(CACHE_CONTROL) == "max-age=${TimeUnit.MINUTES.toSeconds(5)}" as String
    result.headers.get(ETAG) == toHexString(expected.hashCode())
    cache.asMap().size() == 1
    result.rendered(JsonRender).object == expected
    1 * trailProvider.provideTrails() >> Promise.value(expected)

    when: 'second response without etag from handler'
    result = requestFixture.handle(classUnderTest)

    then:
    result.status.code == 200
    result.headers.get(CACHE_CONTROL) == "max-age=${TimeUnit.MINUTES.toSeconds(5)}" as String
    result.headers.get(ETAG) == toHexString(expected.hashCode())
    result.rendered(JsonRender).object == expected

    when: 'second response with etag from handler'
    result = requestFixture.header(IF_NONE_MATCH, toHexString(expected.hashCode()))
        .handle(classUnderTest)

    then:
    result.status.code == 304
  }

  Should "should render json data when v1 header is provided"() {
    given: 'setup handler'
    TrailProvider trailProvider = Mock()
    Cache cache = Caffeine.newBuilder().build()
    def classUnderTest = new TrailHandler(new TrailCache(cache), trailProvider)

    and: 'setup models'
    def expectedTrail = new TrailInfo(
        name: 'Plumbus',
        status: 'soft',
        description: 'Everyone has a Plumbus',
        lastUpdated: LocalDateTime.now()
        )
    def expectedRegion = new TrailRegion(
        name: 'C-137',
        trails: [expectedTrail])
    def expected = [expectedRegion]

    and: 'with 1 as api-version'
    requestFixture.header('api-version', '1')

    when: 'first response from handler'
    def result = requestFixture.handle(classUnderTest)

    then:
    result.status.code == 200
    result.headers.get(CACHE_CONTROL) == "max-age=${TimeUnit.MINUTES.toSeconds(5)}" as String
    result.headers.get(ETAG) == toHexString(expected.hashCode())
    cache.asMap().size() == 1
    result.rendered(JsonRender).object == expected
    1 * trailProvider.provideTrails() >> Promise.value(expected)

    when: 'second response without etag from handler'
    result = requestFixture.handle(classUnderTest)

    then:
    result.status.code == 200
    result.headers.get(CACHE_CONTROL) == "max-age=${TimeUnit.MINUTES.toSeconds(5)}" as String
    result.headers.get(ETAG) == toHexString(expected.hashCode())
    result.rendered(JsonRender).object == expected

    when: 'second response with etag from handler'
    result = requestFixture.header(IF_NONE_MATCH, toHexString(expected.hashCode()))
        .handle(classUnderTest)

    then:
    result.status.code == 304
  }

  Should "should render json data when v2 header is provided"() {
    given: 'setup handler'
    TrailProvider trailProvider = Mock()
    Cache cache = Caffeine.newBuilder().build()
    def classUnderTest = new TrailHandler(new TrailCache(cache), trailProvider)

    and: 'setup models'
    def expectedTrail = new TrailInfo(
        name: 'Plumbus',
        status: 'soft',
        description: 'Everyone has a Plumbus',
        lastUpdated: LocalDateTime.now()
        )
    def expected = [expectedTrail]

    and: 'with 2 as api-version'
    requestFixture.header('api-version', '2')

    when: 'first response from handler'
    def result = requestFixture.handle(classUnderTest)

    then:
    result.status.code == 200
    result.headers.get(CACHE_CONTROL) == "max-age=${TimeUnit.MINUTES.toSeconds(5)}" as String
    result.headers.get(ETAG) == toHexString(expected.hashCode())
    cache.asMap().size() == 1
    result.rendered(JsonRender).object == expected
    1 * trailProvider.provideTrailsV2() >> Promise.value(expected)

    when: 'second response without etag from handler'
    result = requestFixture.handle(classUnderTest)

    then:
    result.status.code == 200
    result.headers.get(CACHE_CONTROL) == "max-age=${TimeUnit.MINUTES.toSeconds(5)}" as String
    result.headers.get(ETAG) == toHexString(expected.hashCode())
    result.rendered(JsonRender).object == expected

    when: 'second response with etag from handler'
    result = requestFixture.header(IF_NONE_MATCH, toHexString(expected.hashCode()))
        .handle(classUnderTest)

    then:
    result.status.code == 304
  }
}
