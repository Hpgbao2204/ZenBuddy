package com.zenbuddy.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.domain.repository.AuthRepository
import com.zenbuddy.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.Login -> login(event.email, event.password)
            is AuthUiEvent.Register -> register(event.email, event.password, event.displayName)
            is AuthUiEvent.SwitchMode -> _state.update { it.copy(isLoginMode = !it.isLoginMode, error = null) }
            is AuthUiEvent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Please fill in all fields") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email, password)
                .onSuccess { user ->
                    _state.update { it.copy(isLoading = false, user = user) }
                    // Sync data from cloud after login
                    syncRepository.syncFromCloud()
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Login failed") }
                }
        }
    }

    private fun register(email: String, password: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _state.update { it.copy(error = "Please fill in all fields") }
            return
        }
        if (password.length < 6) {
            _state.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.register(email, password, displayName)
                .onSuccess { user ->
                    _state.update { it.copy(isLoading = false, user = user, verificationSent = true) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Registration failed") }
                }
        }
    }
}
