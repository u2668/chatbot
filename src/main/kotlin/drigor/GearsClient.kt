package drigor

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class GearsClient {

    val restTemplate = RestTemplate()

    fun sendCard(card: Card) {
        restTemplate.postForObject(
                "http://46.101.204.43:1331/send",
                card,
                Void::class.java
        )
    }

    data class Card(
            val name: String,
            val driver: Boolean? = null,
            val time: String? = null,
            val place: String? = null
    )
}
