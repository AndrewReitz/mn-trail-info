package cash.andrew.mntrailinfo

import cash.andrew.mntrailinfo.model.TrailInfo
import cash.andrew.mntrailinfo.model.TrailRegion
import com.github.benmanes.caffeine.cache.Cache
import groovy.transform.CompileStatic

import javax.inject.Inject
import javax.inject.Singleton

import static com.google.common.base.Preconditions.checkNotNull

@Singleton
@CompileStatic
class TrailCache {

    private static final String CACHE_KEY = 'trailInfo'
    private static final String CACHE_KEY_V2 = 'trailInfoV2'
    private static final String CACHE_KEY_V3 = 'trailInfoV3'

    private final Cache cache

    @Inject TrailCache(Cache cache) {
        this.cache = checkNotNull(cache, 'cache == null')
    }

    void cacheV1Data(List<TrailRegion> trailRegions) {
        cache.put(CACHE_KEY, trailRegions)
    }

    List<TrailRegion> getV1Data() {
        cache.getIfPresent(CACHE_KEY)
    }

    void cacheV2Data(List<TrailInfo> trailInfoList) {
        cache.put(CACHE_KEY_V2, trailInfoList)
    }

    List<TrailInfo> getV2Data() {
        cache.getIfPresent(CACHE_KEY_V2)
    }

    void cacheV3Data(String trailJson) {
        cache.put(CACHE_KEY_V3, trailJson)
    }

    String getV3Data() {
        cache.getIfPresent(CACHE_KEY_V3)
    }
}
