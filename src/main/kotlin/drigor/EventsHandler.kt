package drigor

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
open class EventsHandler(val api: SkypeApi) {

    @EventListener
    fun handleMessage(message: Message) {
        println(message)
        api.sendMessage(message.text, message.from.id)
    }

    @EventListener
    fun handleNewConversation(addedToConversation: AddedToConversation) {
        println(addedToConversation.conversation)
    }
}
