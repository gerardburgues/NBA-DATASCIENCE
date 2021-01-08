package pl.pwr.nbaproject.api

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import pl.pwr.nbaproject.model.api.Teams

@Service
class TeamsClient(
    private val ballDontLieWebClient: WebClient
) {

    suspend fun getTeams(page: Long, perPage: Long = 100): Teams = ballDontLieWebClient.get()
        .uri { uriBuilder ->
            uriBuilder.path("/teams")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .build()
        }
        .retrieve()
        .awaitBody()

}
