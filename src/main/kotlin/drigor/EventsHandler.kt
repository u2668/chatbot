package drigor

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.*

@Service
open class EventsHandler(
        val api: SkypeApi,
        val brain: BrainClient,
        val gears: GearsClient) {

    val logger = LoggerFactory.getLogger(javaClass)

    val explanationPattern = Regex("""^#(place|crap) (.+)""")

    val unknownTerms = HashMap<User, String>()

    @EventListener
    fun handleMessage(message: Message) {
        try {
            logger.info(message.toString())

            explanationPattern.find(message.text)?.let {
                val (category, explanation) = it.destructured
                val s = unknownTerms[message.from]
                logger.info("new term! $explanation is $s — $category")
                brain.explainMessage(s!!, explanation, MessageCategories.valueOf(category.toUpperCase()))
            } ?: let {
                val (messageClass, meta) = brain.askForCategory(message.text)
                logger.info("message recognized as $messageClass")

                when (messageClass) {
                    MessageCategories.PASSENGER -> gears.sendCard(GearsClient.Card(name = message.from.name!!, driver = false))
                    MessageCategories.DRIVER -> gears.sendCard(GearsClient.Card(name = message.from.name!!, driver = true))
                    MessageCategories.TIME -> gears.sendCard(GearsClient.Card(name = message.from.name!!, time = message.text))
                    MessageCategories.PLACE -> {
                        gears.sendCard(GearsClient.Card(name = message.from.name!!, place = meta))
                    }
                    MessageCategories.UNKNOWN -> {
                        unknownTerms[message.from] = message.text
                        api.sendMessage("Эмм... не понял", message.conversation)
                    }
                    MessageCategories.CRAP -> {
                        logger.info("skipping crap")
                    }
                }
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    @EventListener
    fun handleNewConversation(addedToConversation: AddedToConversation) {
        logger.info("bot was added to conversation")
        api.sendMessage("Всем привет!", addedToConversation.conversation)
        println(addedToConversation.conversation)
    }
}
