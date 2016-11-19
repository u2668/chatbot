package drigor

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SchedulerEndpoint(
        val publisher: ApplicationEventPublisher
) {

    val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/notifications")
    @CrossOrigin(origins = arrayOf("*"))
    fun handleNotification(@RequestBody notification: Map<String, Any>) {
        logger.info("$notification")
        publisher.publishEvent(Notification(notification))
    }
}
