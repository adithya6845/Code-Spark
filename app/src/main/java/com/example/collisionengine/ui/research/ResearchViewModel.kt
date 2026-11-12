package com.example.collisionengine.ui.research

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.collisionengine.data.model.ChatMessage
import com.example.collisionengine.data.network.CampusGenieSearchEngine
import com.example.collisionengine.data.network.DatabricksClient
import com.example.collisionengine.data.network.LocalDatasetClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _queryText = MutableStateFlow("")
    val queryText: StateFlow<String> = _queryText.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        CampusGenieSearchEngine.init(application.applicationContext)
        LocalDatasetClient.init(application.applicationContext)
    }

    fun onQueryChanged(newText: String) {
        _queryText.value = newText
    }

    fun askDatabricks() {
        val query = _queryText.value.trim()
        if (query.isBlank()) return

        val userMsg = ChatMessage(text = query, isUser = true)
        _messages.value = _messages.value + userMsg

        _isLoading.value = true
        _queryText.value = ""

        viewModelScope.launch {
            var answerText = ""
            try {
                val liveDatabricks = DatabricksClient.askGenie(query)
                if (liveDatabricks.isNotBlank() && !liveDatabricks.contains("Failed", true) && !liveDatabricks.contains("not directly related", true)) {
                    answerText = liveDatabricks
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (answerText.isBlank()) {
                val genieSearch = CampusGenieSearchEngine.answerQuery(query)
                answerText = genieSearch.text
            }

            val mentionedProfiles = LocalDatasetClient.extractProfilesMentionedInText(answerText)

            val aiMsg = ChatMessage(
                text = answerText,
                isUser = false,
                isTopMatch = mentionedProfiles.isNotEmpty(),
                topMatches = mentionedProfiles
            )
            _messages.value = _messages.value + aiMsg
            _isLoading.value = false
        }
    }
}
