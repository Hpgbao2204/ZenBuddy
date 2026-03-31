package com.zenbuddy.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun syncToCloud() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, syncMessage = null) }
            syncRepository.syncToCloud()
                .onSuccess {
                    _state.update { it.copy(isSyncing = false, syncMessage = "Synced successfully! ☁️") }
                }
                .onFailure { e ->
                    _state.update { it.copy(isSyncing = false, syncMessage = "Sync failed: ${e.message}") }
                }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(syncMessage = null) }
    }
}
