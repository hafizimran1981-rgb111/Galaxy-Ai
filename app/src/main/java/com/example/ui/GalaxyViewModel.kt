package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.data.AppDatabase
import com.example.data.ChatConversation
import com.example.data.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SubscriptionTier(val price: String, val displayName: String, val iconName: String) {
    FREE("$0", "Free Basic", "AutoAwesome"),
    PLUS("$20/mo", "Plus Pro", "Star"),
    TEAM("$25/mo per user", "Team Creator", "Groups")
}

enum class GalaxyTheme(val displayName: String) {
    SLATE_ASTRO("Slate Astro"),
    DEEP_NEBULA("Deep Space Nebula"),
    SOLAR_FLARE("Solar Flare Gold")
}

sealed interface ChatUiState {
    object Idle : ChatUiState
    object Generating : ChatUiState
    data class Success(val message: String) : ChatUiState
    data class Error(val error: String) : ChatUiState
}

class GalaxyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val chatDao = db.chatDao()

    // Active tier
    private val _subscriptionTier = MutableStateFlow(SubscriptionTier.FREE)
    val subscriptionTier: StateFlow<SubscriptionTier> = _subscriptionTier.asStateFlow()

    // Free queries counter
    private val _freeQueryCount = MutableStateFlow(0)
    val freeQueryCount: StateFlow<Int> = _freeQueryCount.asStateFlow()

    // Active visual theme
    private val _activeTheme = MutableStateFlow(GalaxyTheme.SLATE_ASTRO)
    val activeTheme: StateFlow<GalaxyTheme> = _activeTheme.asStateFlow()

    // Active chat conversation ID
    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    // Selected model alias (dynamically resolved)
    val selectedModel: StateFlow<String> = _subscriptionTier.map { tier ->
        when (tier) {
            SubscriptionTier.FREE -> "gemini-3.5-flash"
            else -> "gemini-3.1-pro-preview"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "gemini-3.5-flash")

    // Live list of all past conversations
    val conversations: StateFlow<List<ChatConversation>> = chatDao.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live messages stream for the currently active conversation
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<ChatMessage>> = _activeConversationId
        .flatMapLatest { id ->
            if (id != null) {
                chatDao.getMessagesForConversation(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat UI generating states
    private val _chatUiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val chatUiState: StateFlow<ChatUiState> = _chatUiState.asStateFlow()

    // Workspace simulation data for TEAM users
    val teamWorkspaceMembers = listOf(
        TeamMember("You (Admin)", "hafizimran1981@gmail.com", true, 42),
        TeamMember("Ayesha Khan", "ayesha.k@galaxyai.team", false, 128),
        TeamMember("Leo Reynolds", "l.reynolds@galaxyai.team", false, 95),
        TeamMember("Marcus Cheng", "m.cheng@galaxyai.team", false, 210)
    )

    val teamSharedPrompts = listOf(
        SharedPrompt("Interstellar Code Reviewer", "Analyzes Kotlin/iOS code for standard edge-cases."),
        SharedPrompt("Galaxy UI Copywriter", "Creates space-infused marketing prose."),
        SharedPrompt("System Flow Arch", "Architects KMP logic and diagrams boundaries.")
    )

    fun subscribeToTier(tier: SubscriptionTier) {
        _subscriptionTier.value = tier
        // Change themes automatically in line with tier properties to highlight premium aesthetics immediately
        when (tier) {
            SubscriptionTier.FREE -> {
                _activeTheme.value = GalaxyTheme.SLATE_ASTRO
            }
            SubscriptionTier.PLUS -> {
                _activeTheme.value = GalaxyTheme.DEEP_NEBULA
            }
            SubscriptionTier.TEAM -> {
                _activeTheme.value = GalaxyTheme.SOLAR_FLARE
            }
        }
    }

    fun setTheme(theme: GalaxyTheme) {
        _activeTheme.value = theme
    }

    fun selectConversation(id: Long?) {
        _activeConversationId.value = id
        _chatUiState.value = ChatUiState.Idle
    }

    fun createNewChat() {
        viewModelScope.launch {
            val title = "New Space Inquiry"
            val newId = chatDao.insertConversation(
                ChatConversation(
                    title = title,
                    modelAlias = selectedModel.value,
                    isPremium = _subscriptionTier.value != SubscriptionTier.FREE
                )
            )
            _activeConversationId.value = newId
            _chatUiState.value = ChatUiState.Idle
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            if (_activeConversationId.value == id) {
                _activeConversationId.value = null
            }
            chatDao.deleteConversationById(id)
        }
    }

    fun editConversationTitle(id: Long, newTitle: String) {
        viewModelScope.launch {
            chatDao.updateConversationTitle(id, newTitle)
        }
    }

    fun resetFreeCounter() {
        _freeQueryCount.value = 0
    }

    fun sendMessage(userText: String) {
        if (userText.trim().isEmpty()) return

        val currentConvId = _activeConversationId.value

        viewModelScope.launch {
            var actualConvId = currentConvId
            if (actualConvId == null) {
                // Auto create new conversation on first message
                val title = if (userText.length > 22) userText.take(20) + "..." else userText
                actualConvId = chatDao.insertConversation(
                    ChatConversation(
                        title = title,
                        modelAlias = selectedModel.value,
                        isPremium = _subscriptionTier.value != SubscriptionTier.FREE
                    )
                )
                _activeConversationId.value = actualConvId
            }

            // If FREE, check rate limits
            if (_subscriptionTier.value == SubscriptionTier.FREE) {
                if (_freeQueryCount.value >= 5) {
                    _chatUiState.value = ChatUiState.Error("Daily message limit exceeded on Free Plan. Upgrade to unlock unlimited smart responses!")
                    return@launch
                }
            }

            // Save user message to Room DB
            val userMsg = ChatMessage(conversationId = actualConvId, role = "user", text = userText)
            chatDao.insertMessage(userMsg)

            // Dynamic renaming if the title is still "New Space Inquiry"
            val currentConversations = conversations.value
            val currentConv = currentConversations.find { it.id == actualConvId }
            if (currentConv != null && currentConv.title == "New Space Inquiry") {
                val briefTitle = if (userText.length > 24) userText.take(22) + "..." else userText
                chatDao.updateConversationTitle(actualConvId, briefTitle)
            }

            _chatUiState.value = ChatUiState.Generating

            // If FREE, increment count
            if (_subscriptionTier.value == SubscriptionTier.FREE) {
                _freeQueryCount.value += 1
            }

            // Prepare list of historic messages in target conversation to send as context (up to 8 turns to optimize speed)
            val history = withContext(Dispatchers.IO) {
                // Read from db or collect
                // Fetch direct messages from database asynchronously to maintain background safety
                val msgList = chatDao.getMessagesForConversation(actualConvId).stateIn(this).value
                msgList.takeLast(10) // Take last 10 messages for memory context
            }

            val requestContents = history.map {
                Content(parts = listOf(Part(text = it.text)))
            }

            try {
                // Call actual Retrofit service using Gemini API
                val systemMessage = Content(
                    parts = listOf(
                        Part(
                            text = "You are Galaxy AI, a futuristic and highly intelligent cosmic assistant. " +
                                    "Your current user model is ${selectedModel.value} in ${subscriptionTier.value.displayName} tier. " +
                                    "Keep answers polished, knowledgeable, clear, and optionally cosmic/insightful. Use Markdown bolding or spacing cleanly. " +
                                    "If the user asks about iOS compatibility, inform them beautifully that Galaxy AI uses Compose Multiplatform and a shared Kotlin Multiplatform library core, allowing pure native iOS compilation via standard Xcode."
                        )
                    )
                )

                val mKey = BuildConfig.GEMINI_API_KEY
                if (mKey == "MY_GEMINI_API_KEY" || mKey.isEmpty()) {
                    // Fail gracefully visually if key is not configured by user in secrets
                    val fallbackResponse = "Greetings! I am Galaxy AI operating in simulated mode since no GEMINI_API_KEY was found in your AI Studio secrets environment. Configure your API key to make live Gemini calls. \n\nTo simulate: here is my celestial insight on **$userText**!"
                    val modelMsg = ChatMessage(conversationId = actualConvId, role = "model", text = fallbackResponse)
                    chatDao.insertMessage(modelMsg)
                    _chatUiState.value = ChatUiState.Idle
                    return@launch
                }

                val apiRequest = GenerateContentRequest(
                    contents = requestContents,
                    systemInstruction = systemMessage
                )

                val response = RetrofitClient.service.generateContent(
                    model = selectedModel.value,
                    apiKey = mKey,
                    request = apiRequest
                )

                val bodyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (bodyText != null) {
                    val modelMsg = ChatMessage(conversationId = actualConvId, role = "model", text = bodyText)
                    chatDao.insertMessage(modelMsg)
                    _chatUiState.value = ChatUiState.Idle
                } else {
                    val finishReason = response.candidates?.firstOrNull()?.finishReason ?: "unknown"
                    val errorMsg = ChatMessage(conversationId = actualConvId, role = "error", text = "Celestial connectivity failed. (Reason: $finishReason). Click refresh to retry.")
                    chatDao.insertMessage(errorMsg)
                    _chatUiState.value = ChatUiState.Error("Celestial connectivity failed: $finishReason")
                }

            } catch (e: Exception) {
                val errorMsgText = "Error: ${e.localizedMessage ?: "Connection timed out."}"
                val errorMsg = ChatMessage(conversationId = actualConvId, role = "error", text = errorMsgText)
                chatDao.insertMessage(errorMsg)
                _chatUiState.value = ChatUiState.Error(errorMsgText)
            }
        }
    }
}

data class TeamMember(val name: String, val email: String, val isAdmin: Boolean, val tokensUsed: Int)

data class SharedPrompt(val title: String, val description: String)
