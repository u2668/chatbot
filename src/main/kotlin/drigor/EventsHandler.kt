package drigor

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.properties.Delegates

@Service
open class EventsHandler(
        val skype: SkypeApi,
        val brain: BrainClient,
        val gears: GearsClient) {

    val logger = LoggerFactory.getLogger(javaClass)

    val explanationPattern = Regex("""^#(place|crap|driver|passenger|place|time)\s*(.+)?""")
    val tagPattern = Regex("""<(\w+).*>.+</\1>""")

    var conversation by Delegates.notNull<User>()

    val unknownTerms = HashMap<User, String>()

    @EventListener
    fun handleMessage(message: Message) {
        try {
            logger.info(message.toString())

            conversation = message.conversation

            explanationPattern.find(message.text)?.let {
                val (category, explanation) = it.destructured
                val s = unknownTerms[message.from]
                logger.info("new term! $explanation is $s — $category")
                brain.explainMessage(s!!, explanation, MessageCategories.valueOf(category.toUpperCase()))
                skype.sendMessage("Ага понял, $s — это $category", message.conversation)
            } ?: let {
                val text = tagPattern.replace(message.text, " ")
                val (messageClass, meta) = brain.askForCategory(text)
                logger.info("message recognized as $messageClass $meta")

                when (messageClass) {
                    MessageCategories.PASSENGER -> gears.sendCard(GearsClient.Card(name = message.from.name!!, driver = false))
                    MessageCategories.DRIVER -> gears.sendCard(GearsClient.Card(name = message.from.name!!, driver = true))
                    MessageCategories.TIME -> {
                        val time = meta?.let {
                            val localTime = Instant.ofEpochMilli(meta.toLong() * 1000).atZone(ZoneId.of("Europe/Moscow")).toLocalTime()
                            val timeString = "${localTime.hour}:${localTime.minute}"
                            logger.info("recognized time $localTime $timeString")
                            timeString
                        } ?: text
                        gears.sendCard(GearsClient.Card(name = message.from.name!!, time = time))
                    }
                    MessageCategories.PLACE -> {
                        gears.sendCard(GearsClient.Card(name = message.from.name!!, place = meta))
                    }
                    MessageCategories.UNKNOWN -> {
                        unknownTerms[message.from] = text
                        skype.sendMessage("${message.from.name}, не понял тебя", message.conversation)
                    }
                    MessageCategories.CRAP -> {
                        logger.info("skipping crap")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    @EventListener
    fun handleNotification(notification: Notification) {
        when (notification.map["type"]) {
            "full" -> skype.sendMessage("${notification.map["payload"]}, твоя машина полная", conversation)
            "needTime" -> skype.sendMessage("Когда?", conversation)
            "needPlace" -> skype.sendMessage("Куда?", conversation)
            "newCar" -> {
                val p = (notification.map["passangers"] as List<String>).toSet().joinToString(", ", "пассажиры: ", "\n")
                skype.sendMessage("Машина, $p", conversation)
            }
            "final" -> {
                skype.sendMessage("${notification.map["payload"] }", conversation)
            }

        }
//        skype.sendCard("123", conversation)
    }

    @EventListener
    fun handleNewConversation(addedToConversation: AddedToConversation) {
        logger.info("bot was added to conversation")
        skype.sendMessage("Всем привет!", addedToConversation.conversation)
        logger.info(addedToConversation.conversation.toString())
    }
}
