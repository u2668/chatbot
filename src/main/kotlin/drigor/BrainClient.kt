package drigor

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class BrainClient {
    val restTemplate = RestTemplate()

    fun askForCategory(text: String) = restTemplate
            .getForObject(
                    "http://46.101.204.43:8081/chat-message-classes/${MessageCategories.UNKNOWN}/{text}",
                    BrainResponse::class.java, text
            ).category.first()

    fun explainMessage(text: String, explanation: String, category: MessageCategories) {
        restTemplate
                .postForObject(
                        "http://46.101.204.43:8081/chat-message-classes/{$category}/{text}",
                        mapOf("meta" to explanation),
                        Void::class.java
                )
    }

    data class BrainResponse(
            @JsonProperty("message.class") val category: List<MessageCategories>
    )
}
