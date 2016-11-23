package drigor

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class GearsClient(
        @Value("\${gears}") val gears: String) {

    val restTemplate = RestTemplate()

    fun sendCard(card: Card) {
        restTemplate.postForObject(
                "http://$gears/send",
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
