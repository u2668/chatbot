package drigor

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.*

@Service
open class EventsHandler(
        val skype: SkypeApi,
        val gears: GearsClient,
        val brain: BrainClient) {

    val logger = LoggerFactory.getLogger(javaClass)

    val explanationPattern = Regex("""^#(place|crap|driver|passenger|place|time)\s*(.+)?""")

    val atPattern = Regex("""^(<at .+>.+</at> )?""")

    var conversation: User? = null

    val unknownTerms = HashMap<User, String>()

    @EventListener
    fun handleMessage(message: Message) {
        val text = message.text.replace(atPattern, "")
        try {
            logger.info(message.toString())

            conversation = message.conversation

            explanationPattern.find(text)?.let {
                val (category, explanation) = it.destructured
                val s = unknownTerms[message.from]
                logger.info("new term! $explanation is $s — $category")
                brain.explainMessage(s!!, explanation, MessageCategories.valueOf(category.toUpperCase()))
                skype.sendMessage("Ага понял, $s — это $category", message.conversation)
            } ?: let {
                val (messageClass, meta) = brain.askForCategory(text)
                logger.info("message recognized as $messageClass $meta")

                when (messageClass) {
                    MessageCategories.PASSENGER -> gears.sendCard(GearsClient.Card(name = message.from.name!!, driver = false))
                    MessageCategories.DRIVER -> gears.sendCard(GearsClient.Card(name = message.from.name!!, driver = true))
                    MessageCategories.TIME -> {
                        val time = meta?.let {
                            val localTime = Instant
                                    .ofEpochSecond(meta.toLong())
                                    .atZone(ZoneId.of("Europe/Moscow"))
                                    .toLocalTime()
                                    .truncatedTo(ChronoUnit.MINUTES)
                                    .toString()
                            logger.info("recognized time $localTime")
                            localTime
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
        logger.info("handling notification $notification")
        val type = notification.map["type"] as String
        when (type) {
            "CarIsFull" -> {
                val car = parseCar(notification.map["car"] as Map<String, Any>)
                skype.sendMessage("Машина заполнена этими этими людьми: ${car.driver}, ${car.passengers.joinToString()}. Едут в ${car.place} в ${car.time}", conversation!!)
            }
            "Status" -> {
                val matchResult = notification.map["matchResult"] as Map<String, Any>
                val text = (matchResult["cars"] as List<Map<String, Any>>)
                        .map { parseCar(it) }
                        .joinToString(separator = "\n") { "${it.driver} везет ${it.passengers.joinToString()} в ${it.place} в ${it.time}" }
                val benches = (matchResult["benches"] as List<Map<String, String>>).map { it["name"] }.joinToString()

                skype.sendMessage(text + if (benches.isNotEmpty()) "\nХотят поехать: $benches" else "", conversation!!)
            }
            "NewCar" -> {
                val car = parseCar(notification.map["car"] as Map<String, Any>)
                skype.sendMessage("Новый водитель —  ${car.driver}. Едет в ${car.place} в ${car.time}", conversation!!)
            }
        }
    }

    @EventListener
    fun handleNewConversation(addedToConversation: AddedToConversation) {
        logger.info(addedToConversation.conversation.toString())
        skype.sendMessage("Всем привет!", addedToConversation.conversation)
    }

    fun parseCar(car: Map<String, Any>) = Car(
            driver = car["driver"] as String,
            place = car["place"] as String,
            time = car["time"] as String,
            passengers = car["passangers"] as List<String>)

    data class Car(
            val driver: String,
            val place: String,
            val time: String,
            val passengers: List<String>
    )
}
