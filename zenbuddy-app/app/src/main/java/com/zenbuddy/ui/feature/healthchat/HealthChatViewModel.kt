package com.zenbuddy.ui.feature.healthchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.HealthAiRepository
import com.zenbuddy.domain.repository.StepRepository
import com.zenbuddy.domain.repository.FoodRepository
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
class HealthChatViewModel @Inject constructor(
    private val healthAiRepository: HealthAiRepository,
    private val userProfileRepository: UserProfileRepository,
    private val stepRepository: StepRepository,
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthChatUiState())
    val uiState: StateFlow<HealthChatUiState> = _uiState.asStateFlow()

    private val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun onEvent(event: HealthChatUiEvent) {
        when (event) {
            is HealthChatUiEvent.InputChanged -> _uiState.update { it.copy(currentInput = event.text) }
            HealthChatUiEvent.SendMessage -> sendMessage()
            HealthChatUiEvent.GenerateMealPlan -> generateMealPlan()
            HealthChatUiEvent.GenerateWorkoutPlan -> generateWorkoutPlan()
            HealthChatUiEvent.DismissPlan -> _uiState.update { it.copy(mealPlan = null, workoutPlan = null) }
            HealthChatUiEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun sendMessage() {
        val input = _uiState.value.currentInput.trim()
        if (input.isEmpty()) return

        val userMessage = HealthChatMessage(text = input, isFromUser = true)
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                currentInput = "",
                isGenerating = true,
                error = null
            )
        }

        viewModelScope.launch {
            val profile = getProfileSummary()
            val steps = getTodaySteps()
            val calories = getTodayCalories()

            healthAiRepository.getHealthAdvice(input, profile, steps, calories).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val messages = _uiState.value.messages.toMutableList()
                        if (messages.lastOrNull()?.isFromUser == false) {
                            messages[messages.lastIndex] = HealthChatMessage(text = result.data, isFromUser = false)
                        } else {
                            messages.add(HealthChatMessage(text = result.data, isFromUser = false))
                        }
                        _uiState.update { it.copy(messages = messages, isGenerating = false) }
                    }
                    is Result.Error -> _uiState.update { state ->
                        state.copy(
                            messages = state.messages + HealthChatMessage(
                                text = result.error.message,
                                isFromUser = false
                            ),
                            error = result.error.message,
                            isGenerating = false
                        )
                    }
                    Result.Loading -> {}
                }
            }
        }
    }

    private fun generateMealPlan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPlan = true) }
            val profileResult = userProfileRepository.getProfile().first()
            if (profileResult is Result.Success) {
                val profile = profileResult.data
                healthAiRepository.generateMealPlan(
                    tdee = profile.calculateTDEE(),
                    goalType = profile.goalType,
                    preferences = "Món Việt Nam"
                ).collect { result ->
                    when (result) {
                        is Result.Success -> _uiState.update {
                            it.copy(mealPlan = result.data, isGeneratingPlan = false)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(error = result.error.message, isGeneratingPlan = false)
                        }
                        Result.Loading -> {}
                    }
                }
            }
        }
    }

    private fun generateWorkoutPlan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPlan = true) }
            val profileResult = userProfileRepository.getProfile().first()
            if (profileResult is Result.Success) {
                val profile = profileResult.data
                healthAiRepository.generateWorkoutPlan(
                    profile = "${profile.gender}, ${profile.age} tuổi, ${profile.weightKg}kg, ${profile.heightCm}cm, ${profile.activityLevel}",
                    goalType = profile.goalType
                ).collect { result ->
                    when (result) {
                        is Result.Success -> _uiState.update {
                            it.copy(workoutPlan = result.data, isGeneratingPlan = false)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(error = result.error.message, isGeneratingPlan = false)
                        }
                        Result.Loading -> {}
                    }
                }
            }
        }
    }

    private suspend fun getProfileSummary(): String {
        val result = userProfileRepository.getProfile().first()
        return if (result is Result.Success) {
            val p = result.data
            val goalLabel = when (p.goalType) {
                "lose" -> "giảm cân"
                "gain" -> "tăng cân/tăng cơ"
                else -> "duy trì sức khỏe"
            }
            val activityLabel = when (p.activityLevel) {
                "sedentary" -> "ít vận động"
                "light" -> "vận động nhẹ"
                "moderate" -> "vận động vừa"
                "active" -> "vận động nhiều"
                "very_active" -> "vận động rất nhiều"
                else -> p.activityLevel
            }
            """
                - Giới tính: ${p.gender}
                - Tuổi: ${p.age}
                - Chiều cao/cân nặng: ${p.heightCm.toInt()}cm, ${p.weightKg.toInt()}kg
                - BMI: ${String.format("%.1f", p.calculateBMI())}
                - BMR/TDEE ước tính: ${p.calculateBMR().toInt()} / ${p.calculateTDEE().toInt()} kcal
                - Mục tiêu: $goalLabel
                - Mức vận động: $activityLabel
                - Mục tiêu bước/ngày: ${p.dailyStepGoal}
                - Mục tiêu calories/ngày: ${p.dailyCalorieGoal.toInt()} kcal
            """.trimIndent()
        } else "Chưa có hồ sơ"
    }

    private suspend fun getTodaySteps(): Int {
        val result = stepRepository.getTodaySteps().first()
        return if (result is Result.Success) result.data.steps else 0
    }

    private suspend fun getTodayCalories(): Double {
        val result = foodRepository.getTotalCaloriesByDate(today).first()
        return if (result is Result.Success) result.data else 0.0
    }
}
