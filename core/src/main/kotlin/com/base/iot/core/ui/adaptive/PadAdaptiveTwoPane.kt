package com.base.iot.core.ui.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PadAdaptiveTwoPane(
    primaryContent: @Composable () -> Unit,
    companionContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    showCompanion: Boolean = true,
    primaryWeight: Float = 0.64f
) {
    val spec = LocalPadSpec.current
    val shouldSplit = spec.canShowCompanionPane && companionContent != null && showCompanion

    if (shouldSplit) {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(primaryWeight).fillMaxSize()) {
                primaryContent()
            }
            Box(modifier = Modifier.weight(1f - primaryWeight).fillMaxSize()) {
                companionContent()
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            primaryContent()
        }
    }
}
