package drigor

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
open class EventsHandler(
        val api: SkypeApi,
        val brain: BrainClient,
        val gears: GearsClient) {

    val logger = LoggerFactory.getLogger(javaClass)

    val explanationPattern = Regex("""^#(\w+) (.+)""")

    @EventListener
    fun handleMessage(message: Message) {
        logger.info(message.toString())

        explanationPattern.find(message.text)?.let {
            val (category, explanation) = it.destructured
            logger.info("new term! $explanation means $category")
        } ?: let {


            val messageClass = brain.askForCategory(message.text)
            logger.info("message recognized as $messageClass")

            when (messageClass) {
                MessageCategories.PASSENGER -> gears.sendCard(GearsClient.Card(name = message.from.name!!, driver = false))
                MessageCategories.DRIVER -> gears.sendCard(GearsClient.Card(name = message.from.name!!, driver = true))
                MessageCategories.TIME -> gears.sendCard(GearsClient.Card(name = message.from.name!!, time = "12:34"))
                MessageCategories.PLACE -> gears.sendCard(GearsClient.Card(name = message.from.name!!, place = "Столовка"))
                MessageCategories.UNKNOWN -> {
                    api.sendMessage("Эмм... не понял", message.conversation)
                }
                MessageCategories.CRAP -> {
                    logger.info("skipping crap")
                }
            }
        }
    }

    @EventListener
    fun handleNewConversation(addedToConversation: AddedToConversation) {
        logger.info("bot was added to conversation")
        api.sendMessage("Всем привет!", addedToConversation.conversation)
        println(addedToConversation.conversation)
    }
}
