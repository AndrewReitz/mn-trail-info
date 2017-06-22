package cash.andrew.mntrailinfo

import ratpack.exec.Promise
import retrofit2.http.GET
import retrofit2.http.Query

interface TrailWebsiteProvider {
  @GET('forums/trailconditions.php') Promise<String> getWebsite()
  @GET('forums/showthread.php') Promise<String> trailDetails(@Query('t') String threadId)
}
