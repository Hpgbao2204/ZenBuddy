package com.zenbuddy.ui.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(uiState = uiState, onEvent = viewModel::onEvent, onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ cá nhân") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // TDEE & BMI Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Chỉ số sức khỏe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        HealthMetric("TDEE", "${uiState.tdee.toInt()} kcal")
                        HealthMetric("BMI", String.format("%.1f", uiState.bmi))
                        HealthMetric("BMR", "${uiState.bmr.toInt()} kcal")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getBmiCategory(uiState.bmi),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Name
            OutlinedTextField(
                value = uiState.profile.name,
                onValueChange = { onEvent(ProfileUiEvent.UpdateName(it)) },
                label = { Text("Tên") },
                modifier = Modifier.fillMaxWidth()
            )

            // Age
            OutlinedTextField(
                value = if (uiState.profile.age > 0) uiState.profile.age.toString() else "",
                onValueChange = { onEvent(ProfileUiEvent.UpdateAge(it.toIntOrNull() ?: 0)) },
                label = { Text("Tuổi") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Gender
            Text("Giới tính", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("male" to "Nam", "female" to "Nữ", "other" to "Khác").forEach { (value, label) ->
                    FilterChip(
                        selected = uiState.profile.gender == value,
                        onClick = { onEvent(ProfileUiEvent.UpdateGender(value)) },
                        label = { Text(label) }
                    )
                }
            }

            // Height & Weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.profile.heightCm.toInt().toString(),
                    onValueChange = { onEvent(ProfileUiEvent.UpdateHeight(it.toDoubleOrNull() ?: 0.0)) },
                    label = { Text("Chiều cao (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.profile.weightKg.toInt().toString(),
                    onValueChange = { onEvent(ProfileUiEvent.UpdateWeight(it.toDoubleOrNull() ?: 0.0)) },
                    label = { Text("Cân nặng (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Activity Level
            Text("Mức hoạt động", style = MaterialTheme.typography.labelLarge)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "sedentary" to "Ít vận động (ngồi nhiều)",
                    "light" to "Nhẹ (1-3 ngày/tuần)",
                    "moderate" to "Vừa phải (3-5 ngày/tuần)",
                    "active" to "Năng động (6-7 ngày/tuần)",
                    "very_active" to "Rất năng động (2x/ngày)"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = uiState.profile.activityLevel == value,
                        onClick = { onEvent(ProfileUiEvent.UpdateActivityLevel(value)) },
                        label = { Text(label) }
                    )
                }
            }

            // Goal
            Text("Mục tiêu", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "lose" to "Giảm cân",
                    "maintain" to "Duy trì",
                    "gain" to "Tăng cân"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = uiState.profile.goalType == value,
                        onClick = { onEvent(ProfileUiEvent.UpdateGoalType(value)) },
                        label = { Text(label) }
                    )
                }
            }

            // Step Goal
            OutlinedTextField(
                value = uiState.profile.dailyStepGoal.toString(),
                onValueChange = { onEvent(ProfileUiEvent.UpdateStepGoal(it.toIntOrNull() ?: 10000)) },
                label = { Text("Mục tiêu bước chân/ngày") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Save button
            Button(
                onClick = { onEvent(ProfileUiEvent.SaveProfile) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) "Đang lưu..." else "💾 Lưu hồ sơ")
            }

            if (uiState.saveSuccess) {
                Text("✅ Đã lưu thành công!", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HealthMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

private fun getBmiCategory(bmi: Double): String = when {
    bmi < 18.5 -> "Thiếu cân - Nên tăng cân lành mạnh"
    bmi < 25.0 -> "Cân nặng bình thường - Tuyệt vời!"
    bmi < 30.0 -> "Thừa cân - Nên giảm cân từ từ"
    else -> "Béo phì - Cần điều chỉnh chế độ ăn và tập luyện"
}
