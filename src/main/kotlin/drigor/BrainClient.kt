package drigor

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class BrainClient(
    @Value("\${brain}") val brainIp: String) {

    val restTemplate = RestTemplate()
    val logger = LoggerFactory.getLogger(javaClass)

    fun askForCategory(text: String): Pair<MessageCategories, String?> {
        val brainResponse = restTemplate
                .getForObject(
                        "http://$brainIp/chat-message-classes/${MessageCategories.UNKNOWN}/{text}",
                        BrainResponse::class.java, text
                )
        return Pair(brainResponse.category.first(), brainResponse.meta?.firstOrNull())
    }

    fun explainMessage(text: String, explanation: String, category: MessageCategories) {
        val r = restTemplate
                .postForObject(
                        "http://$brainIp/chat-message-classes/{category}/{text}?meta={meta}",
                        emptyMap<String, String>(),
                        Map::class.java, category, text, explanation
                )
        logger.info(r.toString())
    }

    data class BrainResponse(
            @JsonProperty("message.class") val category: List<MessageCategories>,
            @JsonProperty("message.class.meta") val meta: List<String>?
    )
}
