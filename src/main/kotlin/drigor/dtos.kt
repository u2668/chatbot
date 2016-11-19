package drigor

data class User(val id: String, val name: String?)

enum class MessageCategories {
    CRAP, PASSENGER, DRIVER, UNKNOWN, PLACE, TIME
}

data class Message(val text: String, val from: User, val conversation: User)
data class AddedToConversation(val conversation: User)
