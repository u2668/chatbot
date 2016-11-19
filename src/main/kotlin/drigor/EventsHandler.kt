package drigor

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
open class EventsHandler(val api: SkypeApi, val brain: BrainClient) {

    val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun handleMessage(message: Message) {
        logger.info(message.toString())
        val messageClass = brain.askForClass(message.text)
        logger.info("message recognized as $messageClass")
        api.sendMessage(messageClass.name, message.conversation.id)
    }

    @EventListener
    fun handleNewConversation(addedToConversation: AddedToConversation) {
        println(addedToConversation.conversation)
    }
}
