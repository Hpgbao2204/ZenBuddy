package com.zenbuddy.ui.feature.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenbuddy.domain.model.FoodEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.io.File
import java.util.UUID
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun FoodScannerRoute(
    viewModel: FoodScannerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.onEvent(FoodScannerUiEvent.OpenCamera)
        }
    }

    FoodScannerScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                FoodScannerUiEvent.OpenCamera -> {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.onEvent(event)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
                else -> viewModel.onEvent(event)
            }
        },
        onNavigateBack = onNavigateBack
    )

    if (uiState.isCameraOpen) {
        CameraScreen(
            onPhotoCaptured = { bytes ->
                viewModel.onEvent(FoodScannerUiEvent.PhotoCaptured(bytes))
            },
            onClose = { viewModel.onEvent(FoodScannerUiEvent.CloseCamera) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScannerScreen(
    uiState: FoodScannerUiState,
    onEvent: (FoodScannerUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý thực phẩm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(
                    onClick = { onEvent(FoodScannerUiEvent.ShowAddDialog) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Add, "Thêm thủ công")
                }
                FloatingActionButton(
                    onClick = { onEvent(FoodScannerUiEvent.OpenCamera) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.CameraAlt, "Quét thực phẩm")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hôm nay", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "${uiState.totalCalories.toInt()} kcal",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${uiState.todayFood.size} món ăn đã ghi nhận",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (uiState.isAnalyzing) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                            Text("Đang phân tích hình ảnh bằng AI...")
                        }
                    }
                }
            }

            // Error display
            uiState.error?.let { errorMsg ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "⚠️ Lỗi phân tích",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                errorMsg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { onEvent(FoodScannerUiEvent.DismissError) }) {
                                Text("Đóng")
                            }
                        }
                    }
                }
            }

            // Analyzed food confirmation
            uiState.analyzedFood?.let { food ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🤖 AI nhận diện:", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${food.calories.toInt()} kcal | P: ${food.protein.toInt()}g | C: ${food.carbs.toInt()}g | F: ${food.fat.toInt()}g")
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                TextButton(onClick = { onEvent(FoodScannerUiEvent.ConfirmFood(food)) }) {
                                    Text("✅ Xác nhận")
                                }
                                TextButton(onClick = { onEvent(FoodScannerUiEvent.DismissAnalysis) }) {
                                    Text("❌ Hủy")
                                }
                            }
                        }
                    }
                }
            }

            // Food list
            item {
                if (uiState.todayFood.isNotEmpty()) {
                    Text(
                        "Danh sách đã ăn",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            items(uiState.todayFood, key = { it.id }) { food ->
                FoodItemCard(food = food, onDelete = { onEvent(FoodScannerUiEvent.DeleteFood(food.id)) })
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Manual add dialog
    if (uiState.showAddDialog) {
        ManualFoodDialog(
            onDismiss = { onEvent(FoodScannerUiEvent.DismissAddDialog) },
            onConfirm = { entry -> onEvent(FoodScannerUiEvent.AddManualFood(entry)) }
        )
    }
}

@Composable
private fun FoodItemCard(food: FoodEntry, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${food.calories.toInt()} kcal | P: ${food.protein.toInt()}g C: ${food.carbs.toInt()}g F: ${food.fat.toInt()}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = food.mealType.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ManualFoodDialog(
    onDismiss: () -> Unit,
    onConfirm: (FoodEntry) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm thực phẩm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món ăn") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && calories.isNotBlank()) {
                        onConfirm(
                            FoodEntry(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                calories = calories.toDoubleOrNull() ?: 0.0,
                                protein = protein.toDoubleOrNull() ?: 0.0,
                                carbs = carbs.toDoubleOrNull() ?: 0.0,
                                fat = fat.toDoubleOrNull() ?: 0.0,
                                mealType = "snack",
                                date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && calories.isNotBlank()
            ) { Text("Thêm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
private fun CameraScreen(
    onPhotoCaptured: (ByteArray) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    var isCapturing by remember { mutableStateOf(false) }

    BackHandler { onClose() }

    DisposableEffect(Unit) {
        onDispose {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({ future.get().unbindAll() }, ContextCompat.getMainExecutor(context))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val future = ProcessCameraProvider.getInstance(ctx)
                    future.addListener({
                        val cameraProvider = future.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color.White)
        }

        // Capture button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (!isCapturing) {
                        isCapturing = true
                        val file = File(context.cacheDir, "food_capture_${System.currentTimeMillis()}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                        imageCapture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    val bytes = file.readBytes()
                                    file.delete()
                                    onPhotoCaptured(bytes)
                                    isCapturing = false
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    isCapturing = false
                                }
                            }
                        )
                    }
                },
                containerColor = Color.White,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Chụp ảnh",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        if (isCapturing) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
    }
}
