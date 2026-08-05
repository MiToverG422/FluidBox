package com.mi.fluidbox.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.ButtonDefaults
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.window.WindowDialog

@Composable
fun CouiConfirmDialog(
    show: Boolean,
    title: String?,
    summary: String?,
    negativeText: String,
    positiveText: String,
    onDismissRequest: () -> Unit,
    onNegative: () -> Unit = onDismissRequest,
    onPositive: () -> Unit,
    onDismissFinished: (() -> Unit)? = null,
) {
    WindowDialog(
        show = show,
        title = title,
        summary = summary,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
    ) {
        CouiDialogButtonBar(
            negativeText = negativeText,
            positiveText = positiveText,
            onNegative = onNegative,
            onPositive = onPositive,
        )
    }
}

@Composable
fun CouiDialogButtonBar(
    negativeText: String,
    positiveText: String,
    onNegative: () -> Unit,
    onPositive: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        TextButton(
            text = negativeText,
            onClick = onNegative,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            minHeight = CouiDialogButtonBarMinHeight,
            insideMargin = CouiDialogButtonInsideMargin,
            colors = ButtonDefaults.textButtonColorsBorderless(),
        )
        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 21.dp)
                .width(0.33.dp)
                .fillMaxHeight()
                .background(COUITheme.colorScheme.dividerLine),
        )
        TextButton(
            text = positiveText,
            onClick = onPositive,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            minHeight = CouiDialogButtonBarMinHeight,
            insideMargin = CouiDialogButtonInsideMargin,
            colors = ButtonDefaults.textButtonColorsBorderless(),
        )
    }
}

private val CouiDialogButtonBarMinHeight = 58.dp
private val CouiDialogButtonInsideMargin = PaddingValues(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 22.dp)
