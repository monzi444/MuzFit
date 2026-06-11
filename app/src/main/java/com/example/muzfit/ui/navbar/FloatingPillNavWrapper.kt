package com.example.muzfit.ui.navbar

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.ComposeView

@Composable
fun FloatingPillNavWrapper(
    onTabSelected: (String) -> Unit,
    onFabClick: () -> Unit
) {
    var selectedTabId by rememberSaveable { mutableStateOf("home") }

    FloatingPillNav(
        selectedTabId = selectedTabId,
        onTabSelected = { id ->
            selectedTabId = id
            onTabSelected(id)
        },
        onFabClick = onFabClick
    )
}

/**
 * Static bridge for Java integration.
 */
object FloatingPillNavBridge {
    @JvmStatic
    fun setContent(
        composeView: ComposeView,
        onTabSelected: (String) -> Unit,
        onFabClick: () -> Unit
    ) {
        composeView.setContent {
            FloatingPillNavWrapper(
                onTabSelected = onTabSelected,
                onFabClick = onFabClick
            )
        }
    }
}
