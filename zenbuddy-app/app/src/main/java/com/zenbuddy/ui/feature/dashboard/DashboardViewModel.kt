package com.zenbuddy.ui.feature.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.FoodRepository
import com.zenbuddy.domain.repository.StepRepository
import com.zenbuddy.domain.repository.UserProfileRepository
import com.zenbuddy.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val stepRepository: StepRepository,
    private val foodRepository: FoodRepository,
    private val userProfileRepository: UserProfileRepository,
    private val weatherRepository: WeatherRepository,
    private val locationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<DashboardEffect>()
    val effects: SharedFlow<DashboardEffect> = _effects.asSharedFlow()

    private val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    init {
        loadDashboard()
    }

    fun onEvent(event: DashboardUiEvent) {
        viewModelScope.launch {
            when (event) {
                DashboardUiEvent.RefreshWeather -> fetchWeather()
                DashboardUiEvent.NavigateToSteps -> _effects.emit(DashboardEffect.NavigateToSteps)
                DashboardUiEvent.NavigateToFood -> _effects.emit(DashboardEffect.NavigateToFood)
                DashboardUiEvent.NavigateToExercises -> _effects.emit(DashboardEffect.NavigateToExercises)
                DashboardUiEvent.NavigateToSchedule -> _effects.emit(DashboardEffect.NavigateToSchedule)
                DashboardUiEvent.NavigateToHealthChat -> _effects.emit(DashboardEffect.NavigateToHealthChat)
                DashboardUiEvent.NavigateToProfile -> _effects.emit(DashboardEffect.NavigateToProfile)
            }
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            userProfileRepository.getProfile().collect { result ->
                if (result is Result.Success) {
                    _uiState.update {
                        it.copy(
                            userName = result.data.name.ifEmpty { "Bạn" },
                            stepGoal = result.data.dailyStepGoal,
                            calorieGoal = result.data.dailyCalorieGoal
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            stepRepository.getTodaySteps().collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(todaySteps = result.data, isLoading = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.error.message, isLoading = false)
                    }
                    Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }

        viewModelScope.launch {
            stepRepository.getWeeklySteps().collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(weeklySteps = result.data) }
                }
            }
        }

        viewModelScope.launch {
            foodRepository.getTotalCaloriesByDate(today).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(todayCaloriesIn = result.data) }
                }
            }
        }

        fetchWeather()
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) return@launch

            try {
                locationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        loadWeatherForLocation(location.latitude, location.longitude)
                    } else {
                        // lastLocation is null, request a fresh location
                        requestFreshLocation()
                    }
                }.addOnFailureListener {
                    requestFreshLocation()
                }
            } catch (_: SecurityException) { }
        }
    }

    @Suppress("MissingPermission")
    private fun requestFreshLocation() {
        try {
            val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000L)
                .setMaxUpdates(1)
                .build()
            locationClient.requestLocationUpdates(
                request,
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { loc ->
                            loadWeatherForLocation(loc.latitude, loc.longitude)
                        }
                        locationClient.removeLocationUpdates(this)
                    }
                },
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) { }
    }

    private fun loadWeatherForLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            val result = weatherRepository.getWeather(lat, lon)
            if (result is Result.Success) {
                _uiState.update { state -> state.copy(weather = result.data) }
            }
        }
    }
}
