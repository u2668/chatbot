package drigor

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class SkypeChatbotApplication

fun main(args: Array<String>) {
    SpringApplication.run(SkypeChatbotApplication::class.java, *args)
}
