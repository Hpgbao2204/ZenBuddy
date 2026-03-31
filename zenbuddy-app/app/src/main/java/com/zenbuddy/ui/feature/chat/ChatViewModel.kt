package com.zenbuddy.ui.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.ChatMessage
import com.zenbuddy.domain.repository.ChatRepository
import com.zenbuddy.domain.usecase.chat.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ChatEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var streamingJob: Job? = null
    private val sessionId = UUID.randomUUID().toString()

    init {
        loadMessages()
    }

    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.InputChanged -> _uiState.update { it.copy(inputText = event.text) }
            is ChatUiEvent.Send -> handleSend(event.text)
            ChatUiEvent.CancelGeneration -> {
                streamingJob?.cancel()
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getMessages(sessionId).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update { it.copy(messages = result.data) }
                    is Result.Error -> _uiState.update { it.copy(error = result.error.message) }
                    is Result.Loading -> {}
                }
            }
        }
    }

    private fun handleSend(text: String) {
        if (text.isBlank()) return
        val userMessage = ChatMessage(
            text = text,
            isFromUser = true,
            sessionId = sessionId
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isGenerating = true,
                streamingText = "",
                error = null
            )
        }

        streamingJob = viewModelScope.launch {
            val flow = sendMessageUseCase(text, sessionId)
            flow.collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(streamingText = result.data) }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isGenerating = false, error = result.error.message)
                        }
                    }
                    is Result.Loading -> {}
                }
            }

            // Streaming complete — save the final AI message
            val finalText = _uiState.value.streamingText
            if (finalText.isNotBlank()) {
                val aiMessage = ChatMessage(
                    text = finalText,
                    isFromUser = false,
                    sessionId = sessionId
                )
                chatRepository.saveMessage(aiMessage)
                _uiState.update {
                    it.copy(
                        messages = it.messages + aiMessage,
                        streamingText = "",
                        isGenerating = false
                    )
                }
            }
        }
    }
}
