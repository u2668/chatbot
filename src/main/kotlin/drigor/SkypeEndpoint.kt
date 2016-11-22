package drigor

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SkypeEndpoint(
        val publisher: ApplicationEventPublisher,
        @Value("28:\${id}") val selfId: String) {

    val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/skype")
    fun handleSkypeEventMessage(@RequestBody event: SkypeMessage): HttpEntity<Void> {
        logger.info(event.toString())
        when (event.type) {
            "message" -> {
                publisher.publishEvent(Message(event.text!!, event.from, event.conversation))
            }
            "conversationUpdate" -> {
                if (event.membersAdded?.any { it.id == selfId } ?: false) {
                    publisher.publishEvent(AddedToConversation(event.conversation))
                }
            }
            else -> logger.info(event.type)
        }
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    data class SkypeMessage(
            val type: String,
            val conversation: User,
            val from: User,
            val text: String?,
            val membersAdded: List<User>?
    )
}
