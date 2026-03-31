package com.retro.arcade.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retro.arcade.app.navigation.RetroArcadeNavHost
import com.retro.arcade.core.datastore.AppPreferencesRepository
import com.retro.arcade.core.datastore.UiPreferences
import com.retro.arcade.core.theme.RetroArcadeTheme

@Composable
fun RetroArcadeApp() {
    val context = LocalContext.current.applicationContext
    val preferencesRepository = remember(context) {
        AppPreferencesRepository(context)
    }
    val preferences by preferencesRepository.preferences.collectAsStateWithLifecycle(
        initialValue = UiPreferences()
    )

    RetroArcadeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            RetroArcadeNavHost(
                preferences = preferences,
                preferencesRepository = preferencesRepository
            )
        }
    }
}
