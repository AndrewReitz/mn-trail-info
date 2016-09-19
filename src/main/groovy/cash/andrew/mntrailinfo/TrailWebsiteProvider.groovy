package cash.andrew.mntrailinfo

import ratpack.exec.Promise
import retrofit2.http.GET

interface TrailWebsiteProvider {
  @GET('forums/trailconditions.php') Promise<String> getWebsite()
}
