package com.vijay.tadashi.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.vijay.tadashi.presentation.components.BasicButton
import com.vijay.tadashi.presentation.components.ScreenTitle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Settings screen for selecting and configuring the active AI provider.
 */
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettingsEffect.ShowMessage -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            ScreenTitle(title = "Settings")

            Spacer(modifier = Modifier.height(24.dp))

            var providerExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = providerExpanded,
                onExpandedChange = { providerExpanded = !providerExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    value = providerLabel(uiState.provider),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Provider") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = providerExpanded,
                    onDismissRequest = { providerExpanded = false }
                ) {
                    providerOptions().forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(providerLabel(provider)) },
                            onClick = {
                                providerExpanded = false
                                viewModel.sendAction(SettingsAction.ProviderChanged(provider))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.apiKey,
                onValueChange = { viewModel.sendAction(SettingsAction.ApiKeyChanged(it)) },
                label = { Text("API Key") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.modelName,
                onValueChange = { viewModel.sendAction(SettingsAction.ModelChanged(it)) },
                label = { Text("Model") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Temperature: ${"%.2f".format(uiState.temperature)}")
            Slider(
                value = uiState.temperature,
                onValueChange = { viewModel.sendAction(SettingsAction.TemperatureChanged(it)) },
                valueRange = 0f..1f
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.maxTokens,
                onValueChange = { viewModel.sendAction(SettingsAction.MaxTokensChanged(it)) },
                label = { Text("Max Tokens") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                BasicButton(
                    modifier = Modifier.weight(1f),
                    text = "Save",
                    onClick = { viewModel.sendAction(SettingsAction.SaveClicked) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                BasicButton(
                    modifier = Modifier.weight(1f),
                    text = "Go Back",
                    onClick = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun providerOptions() = listOf(
    com.vijay.tadashi.core.ai.AIProvider.RULE_BASED,
    com.vijay.tadashi.core.ai.AIProvider.GEMINI,
    com.vijay.tadashi.core.ai.AIProvider.OPENAI,
    com.vijay.tadashi.core.ai.AIProvider.OLLAMA
)

private fun providerLabel(provider: com.vijay.tadashi.core.ai.AIProvider): String {
    return when (provider) {
        com.vijay.tadashi.core.ai.AIProvider.RULE_BASED -> "Rule Based"
        com.vijay.tadashi.core.ai.AIProvider.GEMINI -> "Gemini"
        com.vijay.tadashi.core.ai.AIProvider.OPENAI -> "OpenAI"
        com.vijay.tadashi.core.ai.AIProvider.OLLAMA -> "Ollama"
    }
}
