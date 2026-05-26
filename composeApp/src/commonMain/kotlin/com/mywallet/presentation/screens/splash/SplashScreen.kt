package com.mywallet.presentation.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mywallet.domain.repository.UserRepository
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun SplashScreen(
    onSplashFinished: (Boolean) -> Unit,
    userRepository: UserRepository = koinInject()
) {
    val profile by userRepository.profileState.collectAsState()

    LaunchedEffect(Unit) {
        delay(100)
        onSplashFinished(profile.isBiometricEnabled)
    }

    Box(modifier = Modifier.fillMaxSize())
}