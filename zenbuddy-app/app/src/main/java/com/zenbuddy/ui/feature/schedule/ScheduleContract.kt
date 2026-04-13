package com.zenbuddy.ui.feature.schedule

import com.zenbuddy.domain.model.ScheduleEntry
import java.time.LocalDate

data class ScheduleUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val entries: List<ScheduleEntry> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val showAiSchedule: Boolean = false,
    val aiScheduleContent: String? = null,
    val isGeneratingAi: Boolean = false,
    val error: String? = null,

    // Add dialog fields
    val newTitle: String = "",
    val newDescription: String = "",
    val newTime: String = "",
    val newType: String = "general",
    val newReminderEnabled: Boolean = true
)

sealed interface ScheduleUiEvent {
    data class SelectDate(val date: LocalDate) : ScheduleUiEvent
    data object ShowAddDialog : ScheduleUiEvent
    data object DismissAddDialog : ScheduleUiEvent
    data class UpdateTitle(val title: String) : ScheduleUiEvent
    data class UpdateDescription(val desc: String) : ScheduleUiEvent
    data class UpdateTime(val time: String) : ScheduleUiEvent
    data class UpdateType(val type: String) : ScheduleUiEvent
    data class UpdateReminder(val enabled: Boolean) : ScheduleUiEvent
    data object SaveEntry : ScheduleUiEvent
    data class ToggleComplete(val entry: ScheduleEntry) : ScheduleUiEvent
    data class DeleteEntry(val entry: ScheduleEntry) : ScheduleUiEvent
    data object GenerateAiSchedule : ScheduleUiEvent
    data object DismissAiSchedule : ScheduleUiEvent
}
