package drigor

import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Service
import org.springframework.util.CollectionUtils

@Service
class SkypeApi {
    val restTemplate = RestTemplateBuilder().build()
    val logger = LoggerFactory.getLogger(javaClass)

    fun requestToken() =
            restTemplate
                    .postForObject(
                            "https://login.microsoftonline.com/common/oauth2/v2.0/token",
                            CollectionUtils.toMultiValueMap(mapOf(
                                    "client_id" to listOf("dead2c46-acc1-4a10-84f8-87a96a9497d4"),
                                    "client_secret" to listOf("wtyMCWdtsmrzHpgixAZ9QTX"),
                                    "grant_type" to listOf("client_credentials"),
                                    "scope" to listOf("https://graph.microsoft.com/.default"))),
                            Map::class.java)["access_token"]

    fun sendMessage(text: String, to: User) {
        val token = requestToken()
        try {
            restTemplate.postForObject(
                    "https://apis.skype.com/v3/conversations/${to.id}/activities/",
                    HttpEntity(
                            mapOf(
                                    "text" to text,
                                    "type" to "message/text"
                            ),
                            CollectionUtils.toMultiValueMap(mapOf(
                                    "Authorization" to listOf("Bearer $token")
                            ))
                    ),
                    Void::class.java)
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    fun sendCard(text: String, to: User) {
        val token = requestToken()
        try {
            restTemplate.postForObject(
                    "https://apis.skype.com/v3/conversations/${to.id}/activities/",
                    HttpEntity(
                            mapOf(
                                    "type" to "message/card.carousel",
                                    "summary" to "Several hotel offers in Paris",
                                    "text" to "here are hotels....",
                                    "attachments" to listOf(
                                            mapOf(
                                                    "contentType" to "application/vnd.microsoft.card.hero",
                                                    "content" to mapOf(
                                                            "title" to "Hotel Radisson Blu Hotel at Disneyland (r) Paris.",
                                                            "subtitle" to "<a href=\"https://disney.radisson.com\">$71 Today up to 27% off</a><br>Booked in the last 2 hours",
                                                            "text" to "Disneyland paris. 40 Aliee De la Mare dian Houleuse, Magny-le-Hongre, Seine-Marne.",
                                                            "images" to listOf(
                                                                    mapOf(
                                                                            "image" to "https://foo.com/path/image.jpg",
                                                                            "alt" to "hello thumb"
                                                                    )),
                                                            "buttons" to listOf(
                                                                    mapOf(
                                                                            "type" to "imBack",
                                                                            "title" to "Reserve",
                                                                            "value" to "Reserve <context offer=\"...\"/>"
                                                                    ),
                                                                    mapOf(
                                                                            "type" to "opneUrl",
                                                                            "title" to "Website",
                                                                            "value" to "https://disney.radisson.com"
                                                                    ),
                                                                    mapOf(
                                                                            "type" to "call",
                                                                            "title" to "Skype support",
                                                                            "value" to "skype:radisson.disney"
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            ),
                            CollectionUtils.toMultiValueMap(mapOf(
                                    "Authorization" to listOf("Bearer $token")
                            ))
                    ),



                    Void::class.java)
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }
}
