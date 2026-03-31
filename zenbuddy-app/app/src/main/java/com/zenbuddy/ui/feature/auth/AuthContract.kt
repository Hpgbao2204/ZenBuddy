package com.zenbuddy.ui.feature.auth

import com.zenbuddy.domain.model.User

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isLoginMode: Boolean = true,
    val verificationSent: Boolean = false
)

sealed interface AuthUiEvent {
    data class Login(val email: String, val password: String) : AuthUiEvent
    data class Register(val email: String, val password: String, val displayName: String) : AuthUiEvent
    data object SwitchMode : AuthUiEvent
    data object DismissError : AuthUiEvent
}
