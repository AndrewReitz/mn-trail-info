package cash.andrew.mntrailinfo

import cash.andrew.mntrailinfo.model.TrailInfo
import cash.andrew.mntrailinfo.model.TrailRegion
import com.google.inject.Inject
import com.google.inject.Singleton
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler
import ratpack.http.MutableHeaders

import java.util.concurrent.TimeUnit

import static com.google.common.base.Preconditions.checkNotNull
import static io.netty.handler.codec.http.HttpHeaderNames.*
import static java.lang.Integer.toHexString
import static ratpack.jackson.Jackson.json

@Singleton
class TrailHandler extends GroovyHandler {

  private static final String VERSION_HEADER = 'api-version'

  private final TrailCache cache
  private final TrailProvider trailProvider

  @Inject
  TrailHandler(TrailCache cache, TrailProvider trailProvider) {
    this.cache = checkNotNull(cache, 'cache == null')
    this.trailProvider = checkNotNull(trailProvider, 'trailProvider == null')
  }

  @Override
  void handle(GroovyContext context) {
    def apiVersion = context.header(VERSION_HEADER).orElse('')

    if (!apiVersion.isInteger() || apiVersion.toInteger() == 1) {
      handleV1(context)
      return
    }

    if (apiVersion.toInteger() == 3) {
      // todo uncomment when api becomes available.
      // handleV3(context)
      context.with {
        response.headers.add(CONTENT_TYPE, 'application/json')
        response.status(501)
        render json([error: 'Api version 3 is not available yet.'])
      }
      return
    }

    if (apiVersion.toInteger() == 2) {
      handleV2(context)
      return
    }

    context.with {
      response.headers.add(CONTENT_TYPE, 'application/json')
      response.status(400)
      render json([error: 'Bad version header'])
    }
  }

  private void handleV3(GroovyContext context) {
    context.with {
      def cacheData = cache.v3Data

      if (cacheData) {
        def etag = request.headers.get(IF_NONE_MATCH)
        if (toHexString(cacheData.hashCode()) == etag) {
          response.status(304).send()
          return
        }
      }

      trailProvider.provideTrailsV3().map {
        it.body.text
      }.then { bodyText ->
        response.with {
          addHeaders(headers, bodyText)
          cache.cacheV3Data(bodyText)
          render bodyText
        }
      }
    }
  }

  private void handleV2(GroovyContext context) {
    context.with {
      def cachedData = cache.v2Data

      if (cachedData) {
        def etag = request.headers.get(IF_NONE_MATCH)
        if (toHexString(cachedData.hashCode()) == etag) {
          response.status(304).send()
          return
        }

        addHeaders(response.headers, cachedData as List<TrailInfo>)
        render json(cachedData)
        return
      }

      trailProvider.provideTrailsV2().then { List<TrailInfo> trails ->
        addHeaders(response.headers, trails)
        cache.cacheV2Data(trails)
        render json(trails)
      }
    }
  }

  private void handleV1(GroovyContext context) {
    context.with {
      def cachedData = cache.v1Data

      if (cachedData) {
        def etag = request.headers.get(IF_NONE_MATCH)
        if (toHexString(cachedData.hashCode()) == etag) {
          response.status(304).send()
          return
        }

        addHeaders(response.headers, cachedData as List<TrailRegion>)
        render json(cachedData)
        return
      }

      trailProvider.provideTrails().then { List<TrailRegion> trails ->
        addHeaders(response.headers, trails)
        cache.cacheV1Data(trails)
        render json(trails)
      }
    }
  }

  private static void addHeaders(MutableHeaders headers, def cachedData) {
    headers.add(CONTENT_TYPE, 'application/json')
    headers.add(CACHE_CONTROL, "max-age=${TimeUnit.MINUTES.toSeconds(5)}")
    headers.add(ETAG, toHexString(cachedData.hashCode()))
    headers.add(ACCESS_CONTROL_ALLOW_ORIGIN, '*')
    headers.add(ACCESS_CONTROL_ALLOW_HEADERS, VERSION_HEADER)
  }
}
