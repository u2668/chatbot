package drigor

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Endpoint(val publisher: ApplicationEventPublisher) {

    val SELF_ID = "28:dead2c46-acc1-4a10-84f8-87a96a9497d4"
    val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/")
    fun handleSkypeEventMessage(@RequestBody event: SkypeMessage): HttpEntity<Void> {
        logger.info(event.toString())
        when (event.type) {
            "message" -> {
                publisher.publishEvent(Message(event.text!!, event.from, event.conversation))
            }
            "conversationUpdate" -> {
                if (event.membersAdded?.any { it.id == SELF_ID } ?: false) {
                    publisher.publishEvent(AddedToConversation(event.conversation))
                }
            }
            else -> println(event.type)
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
