package drigor

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class BrainClient {
    val restTemplate = RestTemplate()

    fun askForClass(text: String)
            = restTemplate.getForObject("http://46.101.204.43:8081/chat-message-classes/UNKNOWN/{text}", Map::class.java, text)["message.class"]
}
