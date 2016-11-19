package drigor

import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Endpoint {

    @PostMapping("/")
    fun handleSkypeEventMessage(@RequestBody event: Map<String, Any>): HttpEntity<Void> {
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}
