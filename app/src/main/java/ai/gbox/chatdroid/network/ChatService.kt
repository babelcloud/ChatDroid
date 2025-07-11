package ai.gbox.chatdroid.network

import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.GET

interface ChatService {
    @GET("chats/")
    suspend fun getChats(): Response<List<ChatListItem>>
}

data class ChatListItem(
    val id: String,
    val title: String,
    @Json(name = "updated_at") val updatedAt: Int,
    @Json(name = "created_at") val createdAt: Int
) 