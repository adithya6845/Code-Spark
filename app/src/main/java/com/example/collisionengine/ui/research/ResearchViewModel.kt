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

    private val appContext = application.applicationContext

    private val _queryText = MutableStateFlow("")
    val queryText: StateFlow<String> = _queryText.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        CampusGenieSearchEngine.init(appContext)
        LocalDatasetClient.init(appContext)
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
                // 1. Query live Databricks Genie
                val liveDatabricks = DatabricksClient.askGenie(query, appContext)
                if (liveDatabricks.isNotBlank() && !liveDatabricks.contains("Failed", true)) {
                    answerText = liveDatabricks
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Fallback to CampusGenieSearchEngine
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
