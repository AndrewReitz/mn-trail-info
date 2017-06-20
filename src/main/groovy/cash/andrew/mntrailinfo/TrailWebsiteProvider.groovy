package cash.andrew.mntrailinfo

import ratpack.exec.Promise
import retrofit2.http.GET
import retrofit2.http.Path

interface TrailWebsiteProvider {
  @GET('forums/trailconditions.php') Promise<String> getWebsite()
  @GET('forums/showthread.php?{trailId}') Promise<Object> getDetails(@Path("trailId") trailId)
}
