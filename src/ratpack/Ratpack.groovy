import cash.andrew.mntrailinfo.TrailHandler
import cash.andrew.mntrailinfo.TrailProvider
import cash.andrew.mntrailinfo.TrailWebsiteDateParser
import cash.andrew.mntrailinfo.TrailWebsiteProvider
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import ratpack.rx.RxRatpack
import ratpack.service.Service
import ratpack.service.StartEvent
import rx.Observable

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
    bindInstance(Cache, Caffeine.newBuilder()
            .maximumSize(1)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build())
    bindInstance(new Service() {
      @Override void onStart(StartEvent event) throws Exception {
        def cache = event.registry.get(Cache)
        def trailProvider  = Observable.just(event.registry.get(TrailProvider)).cache()


        def interval = Observable.interval(5, TimeUnit.MINUTES)

        Observable.just(-1L) // start event
                .mergeWith(interval)
                .observeOn(RxRatpack.scheduler())
                .flatMap { trailProvider }
                .flatMap { it.provideTrails().observe() }
                .subscribe {
                  cache.put('trailInfo', it)
                }
      }
    })
  }
  handlers {
    get(TrailHandler)
  }
}
