package cash.andrew.mntrailinfo

import ratpack.exec.Promise
import ratpack.http.client.HttpClient

import javax.inject.Inject
import javax.inject.Singleton

import static com.google.common.base.Preconditions.checkNotNull

@Singleton
class TrailWebsiteProvider {

  private static final URI TRAIL_INDEX = URI.create('http://www.morcmtb.org/forums/trailconditions.php')

  private final HttpClient httpClient

  @Inject TrailWebsiteProvider(HttpClient httpClient) {
    this.httpClient = checkNotNull(httpClient, 'httpClient == null')
  }

  Promise<String> getWebsite() {
    httpClient.get(TRAIL_INDEX).map { response -> response.body.text }
  }

  Promise<String> trailDetails(String threadId) {
    def uri = URI.create("http://www.morcmtb.org/forums/showthread.php?$threadId")
    httpClient.get(uri).map { response -> response.body.text }
  }
}
