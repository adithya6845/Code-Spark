package com.example.collisionengine.ui.placement

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
import java.util.Locale

class PlacementViewModel(application: Application) : AndroidViewModel(application) {

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
            val qLower = query.lowercase(Locale.ROOT)
            val isWhatIfQuery = qLower.contains("what if") || qLower.contains("what is") ||
                    qLower.contains("build") || qLower.contains("team") || qLower.contains("collaborat") ||
                    qLower.contains("choose") || qLower.contains("switch") || qLower.contains("drone") ||
                    qLower.contains("robot") || qLower.contains("hackathon")

            var answerText = ""

            if (isWhatIfQuery) {
                // Generate What-If Decision Engine simulation
                val simulation = CampusGenieSearchEngine.answerQuery(query)
                answerText = simulation.text
            } else {
                // Direct database question
                try {
                    val liveDatabricks = DatabricksClient.askGenie(query, getApplication<Application>().applicationContext)
                    if (liveDatabricks.isNotBlank() && !liveDatabricks.contains("Failed", true) && !liveDatabricks.contains("not directly related", true)) {
                        answerText = liveDatabricks
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (answerText.isBlank()) {
                    val simulation = CampusGenieSearchEngine.answerQuery(query)
                    answerText = simulation.text
                }
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
