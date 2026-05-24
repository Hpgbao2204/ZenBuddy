package com.zenbuddy.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.domain.model.User
import com.zenbuddy.domain.repository.AuthRepository
import com.zenbuddy.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val user: User? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
    }

    fun syncToCloud() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, syncMessage = null) }
            syncRepository.syncToCloud()
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSyncing = false,
                            syncMessage = "Local mode: data is already saved on this device."
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isSyncing = false, syncMessage = "Sync failed: ${e.message}") }
                }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(syncMessage = null) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
