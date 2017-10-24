package cash.andrew.mntrailinfo

import groovy.transform.CompileStatic
import ratpack.exec.Execution
import ratpack.service.Service
import ratpack.service.StartEvent

import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Singleton
@CompileStatic
class TrailCachingService implements Service {

    private TrailCache cache
    private TrailProvider trailProvider

    @Override void onStart(StartEvent e) {
        this.cache = e.registry.get(TrailCache)
        this.trailProvider = e.registry.get(TrailProvider)
        run()
    }

    private void run() {
        Execution.fork().onComplete {
            Execution.current().controller.executor.schedule(this.&run, 4, TimeUnit.MINUTES)
        }
        .start { e ->
            trailProvider.provideTrails().then { cache.cacheV1Data(it) }
            trailProvider.provideTrailsV2().then { cache.cacheV2Data(it) }
        }
    }
}
