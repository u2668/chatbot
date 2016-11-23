package drigor

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.event.SimpleApplicationEventMulticaster
import java.util.concurrent.Executors

@SpringBootApplication
open class SkypeChatbotApplication {
    @Bean
    open fun applicationEventMulticaster() = SimpleApplicationEventMulticaster().apply {
        this.setTaskExecutor(Executors.newCachedThreadPool())
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(SkypeChatbotApplication::class.java, *args)
}
