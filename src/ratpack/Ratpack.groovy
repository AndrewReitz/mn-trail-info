import cash.andrew.mntrailinfo.TrailCache
import cash.andrew.mntrailinfo.TrailCachingService
import cash.andrew.mntrailinfo.TrailHandler
import cash.andrew.mntrailinfo.TrailProvider
import cash.andrew.mntrailinfo.TrailWebsiteDateParser
import cash.andrew.mntrailinfo.TrailWebsiteProvider
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine

import java.util.concurrent.TimeUnit

import static ratpack.groovy.Groovy.ratpack

ratpack {
  serverConfig {
    env()
  }
  bindings {
    bind(TrailHandler)
    bind(TrailWebsiteDateParser)
    bind(TrailWebsiteProvider)
    bind(TrailProvider)
    bind(TrailCache)
    bind(TrailCachingService)
    bindInstance(Cache, Caffeine.newBuilder()
            .maximumSize(1)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build())
  }
  handlers {
    get(TrailHandler)
  }
}
