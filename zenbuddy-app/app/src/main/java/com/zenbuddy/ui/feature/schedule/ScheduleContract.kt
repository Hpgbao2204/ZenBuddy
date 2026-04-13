package com.zenbuddy.ui.feature.schedule

import com.zenbuddy.domain.model.ScheduleEntry
import java.time.LocalDate

data class ScheduleUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val entries: List<ScheduleEntry> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val showAiSchedule: Boolean = false,
    val aiScheduleEntries: List<ScheduleEntry> = emptyList(),
    val isGeneratingAi: Boolean = false,
    val error: String? = null,

    // Add dialog fields
    val newTitle: String = "",
    val newDescription: String = "",
    val newTimeHour: Int = 8,
    val newTimeMinute: Int = 0,
    val newType: String = "custom",
    val newReminderEnabled: Boolean = true
)

sealed interface ScheduleUiEvent {
    data class SelectDate(val date: LocalDate) : ScheduleUiEvent
    data object ShowAddDialog : ScheduleUiEvent
    data object DismissAddDialog : ScheduleUiEvent
    data class UpdateTitle(val title: String) : ScheduleUiEvent
    data class UpdateDescription(val desc: String) : ScheduleUiEvent
    data class UpdateTimeHour(val hour: Int) : ScheduleUiEvent
    data class UpdateTimeMinute(val minute: Int) : ScheduleUiEvent
    data class UpdateType(val type: String) : ScheduleUiEvent
    data class UpdateReminder(val enabled: Boolean) : ScheduleUiEvent
    data object SaveEntry : ScheduleUiEvent
    data class ToggleComplete(val entry: ScheduleEntry) : ScheduleUiEvent
    data class DeleteEntry(val entry: ScheduleEntry) : ScheduleUiEvent
    data object GenerateAiSchedule : ScheduleUiEvent
    data object DismissAiSchedule : ScheduleUiEvent
}
