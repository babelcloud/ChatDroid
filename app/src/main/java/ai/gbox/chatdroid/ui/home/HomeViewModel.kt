package ai.gbox.chatdroid.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.gbox.chatdroid.network.ChatListItem
import ai.gbox.chatdroid.repository.ChatRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repo = ChatRepository()

    private val _chats = MutableLiveData<List<ChatListItem>>()
    val chats: LiveData<List<ChatListItem>> = _chats

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    init {
        Log.d("HomeViewModel", "Initializing HomeViewModel")
        loadChats()
    }

    fun loadChats() {
        Log.d("HomeViewModel", "Starting to load chats")
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val result = repo.fetchChats()
                result.fold(onSuccess = { chatList ->
                    Log.d("HomeViewModel", "Successfully loaded ${chatList.size} chats")
                    _chats.postValue(chatList)
                    _error.postValue(null)
                }, onFailure = { exception ->
                    Log.e("HomeViewModel", "Failed to load chats", exception)
                    _error.postValue("Failed to load chats: ${exception.message}")
                })
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception in loadChats", e)
                _error.postValue("Unexpected error: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }
}