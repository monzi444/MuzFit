package com.example.muzfit.ui.quick

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy

object QuickOverlayHelper {
    var showOverlay by mutableStateOf(false)
        private set

    fun init(composeView: ComposeView) {
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                QuickOverlay(
                    show = showOverlay,
                    onDismiss = { hide() }
                )
            }
        }
    }

    @JvmStatic
    fun toggle() {
        showOverlay = !showOverlay
    }

    @JvmStatic
    fun hide() {
        showOverlay = false
    }

    @JvmStatic
    fun isVisible(): Boolean {
        return showOverlay
    }
}
