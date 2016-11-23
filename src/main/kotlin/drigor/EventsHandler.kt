package drigor

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.LocalTime

@Service
open class EventsHandler(
        val skype: SkypeApi,
        val gears: GearsClient) {

    val logger = LoggerFactory.getLogger(javaClass)
    val driverPattern = Regex("""^(<at .+>.+</at> )?Везу в (\d{1,2}):(\d{2}) в (.+)$""")
    val passengerPattern = Regex("""^(<at .+>.+</at> )?\+$""")
    var conversation: User? = null

    @EventListener
    fun handleMessage(message: Message) {
        conversation = message.conversation
        try {
            logger.info(message.toString())
            when {
                message.text.matches(passengerPattern) -> {
                    gears.sendCard(GearsClient.Card(name = message.from.name!!, driver = false))
                }
                message.text.matches(driverPattern) -> {
                    val (skip, hours, minutes, place) = driverPattern.find(message.text)!!.destructured
                    val time = LocalTime.of(hours.toInt(), minutes.toInt())
                    gears.sendCard(GearsClient.Card(name = message.from.name!!, driver = true, time = time.toString(), place = place))
                }
                else -> {
                    skype.sendMessage("Мне жаль... я вас не понял", message.conversation)
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
