package ai.gbox.chatdroid.repository

import android.util.Log
import ai.gbox.chatdroid.network.ApiClient
import ai.gbox.chatdroid.network.ChatListItem
import ai.gbox.chatdroid.network.ChatService
import ai.gbox.chatdroid.datastore.AuthPreferences
import com.squareup.moshi.JsonDataException
import retrofit2.HttpException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

class ChatRepository {
    private val api: ChatService by lazy { ApiClient.create(ChatService::class.java) }

    suspend fun fetchChats(): Result<List<ChatListItem>> {
        // First, let's try to get the raw response for debugging
        debugRawApiResponse()
        
        return try {
            Log.d("ChatRepository", "Making API call to fetch chats")
            val response = api.getChats()
            
            Log.d("ChatRepository", "API Response Code: ${response.code()}")
            Log.d("ChatRepository", "API Response Message: ${response.message()}")
            
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                Log.d("ChatRepository", "API call successful, got ${list.size} chats")
                list.forEach { chat ->
                    Log.d("ChatRepository", "Chat: id=${chat.id}, title=${chat.title}")
                }
                Result.success(list)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRepository", "API returned error: ${response.code()} ${response.message()}")
                Log.e("ChatRepository", "Error body: $errorBody")
                Result.failure(Exception("API error: ${response.code()} ${response.message()}. Body: $errorBody"))
            }
        } catch (e: JsonDataException) {
            Log.e("ChatRepository", "JSON parsing error: ${e.message}", e)
            Result.failure(Exception("JSON parsing error: ${e.message}. The API response format may not match expected structure."))
        } catch (e: HttpException) {
            Log.e("ChatRepository", "HTTP error: ${e.code()} ${e.message()}", e)
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("ChatRepository", "Error response body: $errorBody")
            } catch (ex: Exception) {
                Log.e("ChatRepository", "Could not read error body", ex)
            }
            Result.failure(Exception("API error: ${e.code()} ${e.message()}"))
        } catch (e: Exception) {
            Log.e("ChatRepository", "API call failed", e)
            Result.failure(e)
        }
    }

    private fun debugRawApiResponse() {
        try {
            val token = AuthPreferences.currentToken()
            Log.d("ChatRepository", "Token: $token")
            
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()

            val request = Request.Builder()
                .url("http://34.121.157.227:3000/api/v1/chats")
                .apply {
                    if (!token.isNullOrBlank()) {
                        header("Authorization", "Bearer $token")
                    }
                }
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d("ChatRepository", "Raw API Response Code: ${response.code}")
            Log.d("ChatRepository", "Raw API Response: $responseBody")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Debug raw API call failed", e)
        }
    }
} 