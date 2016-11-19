package drigor

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

    @PostMapping("/")
    fun handleSkypeEventMessage(@RequestBody event: SkypeMessage): HttpEntity<Void> {
        when (event.type) {
            "message" -> {
                publisher.publishEvent(Message(event.text!!, event.from, event.conversation))
            }
            "conversationUpdate" -> {
                if (event.membersAdded!!.any { it.id == SELF_ID }) {
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
