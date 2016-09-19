package cash.andrew.mntrailinfo

import cash.andrew.mntrailinfo.model.TrailRegion
import com.github.benmanes.caffeine.cache.Cache
import com.google.inject.Inject
import com.google.inject.Singleton
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.http.MutableHeaders

import java.util.concurrent.TimeUnit

import static com.google.common.base.Preconditions.checkNotNull
import static io.netty.handler.codec.http.HttpHeaderNames.*
import static java.lang.Integer.toHexString
import static ratpack.jackson.Jackson.json

@Singleton
class TrailHandler implements Handler {
  private static final String CACHE_KEY = 'trailInfo'

  private final Cache cache
  private final TrailProvider trailProvider

  @Inject TrailHandler(Cache cache, TrailProvider trailProvider) {
    this.cache = checkNotNull(cache, 'cache == null')
    this.trailProvider = checkNotNull(trailProvider, 'trailProvider == null')
  }

  @Override void handle(Context context) {
    def request = context.request
    def response = context.response
    def render = context.&render

    def cachedData = cache.getIfPresent(CACHE_KEY)

    if (cachedData) {
      def etag = request.headers.get(IF_NONE_MATCH)
      if (toHexString(cachedData.hashCode()) == etag) {
        response.status(304).send()
        return
      }

      addCacheHeaders(response.headers, cachedData as List<TrailRegion>)
      render json(cachedData)
      return
    }

    trailProvider.provideTrails().then { List<TrailRegion> trails ->
      addCacheHeaders(response.headers, trails)
      cache.put(CACHE_KEY, trails)
      render json(trails)
    }
  }

  private static void addCacheHeaders(MutableHeaders headers, List<TrailRegion> trails) {
    headers.add(CACHE_CONTROL, "max-age=${TimeUnit.MINUTES.toSeconds(5)}")
    headers.add(ETAG, toHexString(trails.hashCode()))
  }
}
