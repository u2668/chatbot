package drigor

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.LocalTime
import java.util.*

@Service
open class EventsHandler(val skype: SkypeApi) {

    data class Car(val driver: User, val time: LocalTime, val place: String) {
        val passengers = ArrayList<User>()

        override fun toString()
                = "В $time ${driver.name} везет в $place. Пассажиры: ${passengers.map { it.name }.joinToString()}"
    }

    val logger = LoggerFactory.getLogger(javaClass)
    val driverPattern = Regex("""^(<at .+>.+</at> )?Везу в (\d{1,2}):(\d{2}) в (.+)$""")
    val passengerPattern = Regex("""^(<at .+>.+</at> )?\+$""")
    var currentCar: Car? = null

    @EventListener
    fun handleMessage(message: Message) {
        try {
            logger.info(message.toString())
            when {
                message.text.matches(passengerPattern) -> {
                    currentCar?.run {
                        passengers.add(message.from)
                        skype.sendMessage(toString(), message.conversation)
                    } ?: skype.sendMessage("Ехать не с кем", message.conversation)
                }
                message.text.matches(driverPattern) -> {
                    val (skip, hours, minutes, place) = driverPattern.find(message.text)!!.destructured
                    val time = LocalTime.of(hours.toInt(), minutes.toInt())
                    currentCar = Car(message.from, time, place)
                    skype.sendMessage(currentCar.toString(), message.conversation)
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
    fun handleNewConversation(addedToConversation: AddedToConversation) {
        logger.info(addedToConversation.conversation.toString())
        skype.sendMessage("Всем привет!", addedToConversation.conversation)
    }
}
