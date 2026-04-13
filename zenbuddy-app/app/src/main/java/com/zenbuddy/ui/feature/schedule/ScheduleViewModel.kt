package com.zenbuddy.ui.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.ScheduleEntry
import com.zenbuddy.domain.repository.HealthAiRepository
import com.zenbuddy.domain.repository.ScheduleRepository
import com.zenbuddy.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val healthAiRepository: HealthAiRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadEntries()
    }

    fun onEvent(event: ScheduleUiEvent) {
        when (event) {
            is ScheduleUiEvent.SelectDate -> {
                _uiState.update { it.copy(selectedDate = event.date) }
                loadEntries()
            }
            ScheduleUiEvent.ShowAddDialog -> _uiState.update { it.copy(showAddDialog = true) }
            ScheduleUiEvent.DismissAddDialog -> _uiState.update {
                it.copy(showAddDialog = false, newTitle = "", newDescription = "", newTime = "", newType = "general", newReminderEnabled = true)
            }
            is ScheduleUiEvent.UpdateTitle -> _uiState.update { it.copy(newTitle = event.title) }
            is ScheduleUiEvent.UpdateDescription -> _uiState.update { it.copy(newDescription = event.desc) }
            is ScheduleUiEvent.UpdateTime -> _uiState.update { it.copy(newTime = event.time) }
            is ScheduleUiEvent.UpdateType -> _uiState.update { it.copy(newType = event.type) }
            is ScheduleUiEvent.UpdateReminder -> _uiState.update { it.copy(newReminderEnabled = event.enabled) }
            ScheduleUiEvent.SaveEntry -> saveEntry()
            is ScheduleUiEvent.ToggleComplete -> toggleComplete(event.entry)
            is ScheduleUiEvent.DeleteEntry -> deleteEntry(event.entry)
            ScheduleUiEvent.GenerateAiSchedule -> generateAiSchedule()
            ScheduleUiEvent.DismissAiSchedule -> _uiState.update { it.copy(showAiSchedule = false, aiScheduleContent = null) }
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            val dateStr = _uiState.value.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            scheduleRepository.getEntriesByDate(dateStr).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update { it.copy(entries = result.data, isLoading = false) }
                    is Result.Error -> _uiState.update { it.copy(error = result.error.message, isLoading = false) }
                    Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun saveEntry() {
        val state = _uiState.value
        if (state.newTitle.isBlank()) return

        val entry = ScheduleEntry(
            date = state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            time = state.newTime.ifBlank { "08:00" },
            title = state.newTitle,
            description = state.newDescription,
            type = state.newType,
            isCompleted = false,
            reminderEnabled = state.newReminderEnabled
        )

        viewModelScope.launch {
            scheduleRepository.addEntry(entry)
            _uiState.update {
                it.copy(showAddDialog = false, newTitle = "", newDescription = "", newTime = "", newType = "general", newReminderEnabled = true)
            }
        }
    }

    private fun toggleComplete(entry: ScheduleEntry) {
        viewModelScope.launch {
            scheduleRepository.updateEntry(entry.copy(isCompleted = !entry.isCompleted))
        }
    }

    private fun deleteEntry(entry: ScheduleEntry) {
        viewModelScope.launch {
            scheduleRepository.deleteEntry(entry)
        }
    }

    private fun generateAiSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingAi = true, showAiSchedule = true) }

            val profileResult = userProfileRepository.getProfile().first()
            val profile = if (profileResult is Result.Success) {
                val p = profileResult.data
                "${p.gender}, ${p.age} tuổi, ${p.weightKg}kg, mục tiêu: ${p.goalType}, hoạt động: ${p.activityLevel}"
            } else "Không có thông tin"

            val existingEntries = _uiState.value.entries.joinToString("; ") { "${it.time} - ${it.title}" }

            healthAiRepository.generateDailySchedule(profile, existingEntries).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(aiScheduleContent = result.data, isGeneratingAi = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.error.message, isGeneratingAi = false)
                    }
                    Result.Loading -> {}
                }
            }
        }
    }
}
