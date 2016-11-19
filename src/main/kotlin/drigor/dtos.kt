package drigor

data class User(val id: String, val name: String?)

data class Message(val text: String, val from: User, val conversation: User)
data class AddedToConversation(val conversation: User)
