package com.vijay.tadashi.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.vijay.tadashi.presentation.components.BasicButton
import com.vijay.tadashi.presentation.components.ScreenTitle
import com.vijay.tadashi.presentation.navigation.Screen

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            ScreenTitle(title = "Welcome to TADASHI!")
            Spacer(modifier = Modifier.height(32.dp))
            BasicButton(
                text = "Go to Settings",
                onClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
    }
}