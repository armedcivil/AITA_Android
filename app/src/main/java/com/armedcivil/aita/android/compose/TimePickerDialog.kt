package com.armedcivil.aita.android.compose

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.armedcivil.aita.android.ui.theme.AITA_AndroidTheme

@ExperimentalMaterial3Api
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton:
        @Composable()
        () -> Unit,
    content:
        @Composable()
        (() -> Unit)?,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier =
                Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
                    .background(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                    ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content?.invoke()
                Row(
                    modifier =
                        Modifier
                            .height(40.dp)
                            .fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    confirmButton()
                }
            }
        }
    }
}

@Preview(widthDp = 320, showBackground = true)
@ExperimentalMaterial3Api
@Composable
fun TimePickerDialogPreview() {
    AITA_AndroidTheme {
        TimePickerDialog(onDismissRequest = { /*TODO*/ }, confirmButton = {
            TextButton(onClick = { }) {
                Text(stringResource(R.string.ok))
            }
        }) {
            TimePicker(state = rememberTimePickerState())
        }
    }
}
