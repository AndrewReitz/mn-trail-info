package cash.andrew.mntrailinfo

import groovy.transform.CompileStatic
import ratpack.exec.Promise
import ratpack.http.client.HttpClient
import ratpack.http.client.ReceivedResponse

import javax.inject.Inject
import javax.inject.Singleton

import static com.google.common.base.Preconditions.checkNotNull

@Singleton
@CompileStatic
class TrailWebsiteProvider {

	private static final URI TRAIL_INDEX = URI.create('http://www.morcmtb.org/forums/trailconditions.php')

	private final HttpClient httpClient

	@Inject TrailWebsiteProvider(HttpClient httpClient) {
		this.httpClient = checkNotNull(httpClient, 'httpClient == null')
	}

	Promise<String> getWebsite() {
		httpClient.get(TRAIL_INDEX).map { ReceivedResponse response -> response.body.text }
	}

	Promise<String> trailDetails(String threadId) {
		def uri = URI.create("http://www.morcmtb.org/forums/showthread.php?$threadId")
		httpClient.get(uri).map { ReceivedResponse response -> response.body.text }
	}
}
