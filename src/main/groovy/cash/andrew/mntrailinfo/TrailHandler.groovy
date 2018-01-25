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
	private final TrailCache cache
	private final TrailProvider trailProvider

	@Inject
	TrailHandler(TrailCache cache, TrailProvider trailProvider) {
		this.cache = checkNotNull(cache, 'cache == null')
		this.trailProvider = checkNotNull(trailProvider, 'trailProvider == null')
	}

	@Override
	void handle(GroovyContext context) {
		def apiVersion = context.header('Api-Version').orElse('')

		// fall through for v1
		if (apiVersion.isInteger() && apiVersion.toInteger() >= 2) {
			handleV2(context)
			return
		}

		handleV1(context)
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

				addCacheHeaders(response.headers, cachedData as List<TrailInfo>)
				render json(cachedData)
				return
			}

			trailProvider.provideTrailsV2().then { List<TrailInfo> trails ->
				addCacheHeaders(response.headers, trails)
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

				addCacheHeaders(response.headers, cachedData as List<TrailRegion>)
				render json(cachedData)
				return
			}

			trailProvider.provideTrails().then { List<TrailRegion> trails ->
				addCacheHeaders(response.headers, trails)
				cache.cacheV1Data(trails)
				render json(trails)
			}
		}
	}

	private static void addCacheHeaders(MutableHeaders headers, def trails) {
		headers.add(CACHE_CONTROL, "max-age=${TimeUnit.MINUTES.toSeconds(5)}")
		headers.add(ETAG, toHexString(trails.hashCode()))
        headers.add(ACCESS_CONTROL_ALLOW_ORIGIN, '*')
	}
}
