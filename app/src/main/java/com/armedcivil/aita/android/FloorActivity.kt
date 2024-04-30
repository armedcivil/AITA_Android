package com.armedcivil.aita.android

import android.R
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.armedcivil.aita.android.compose.TimePickerDialog
import com.armedcivil.aita.android.data.SceneObject
import com.armedcivil.aita.android.http_client.ApiClient
import com.armedcivil.aita.android.http_client.response.FloorResponse
import com.armedcivil.aita.android.http_client.response.GetReservationResponse
import com.armedcivil.aita.android.ui.theme.AITA_AndroidTheme
import com.armedcivil.aita.android.view.AITAViewerSurface
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar

// TODO:予約一覧・予約画面を別activityに分ける
class FloorActivity : ComponentActivity() {
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AITA_AndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val state =
                        ApiClient.instance.floorResponseObservable.subscribeAsState(
                            initial =
                                FloorResponse(
                                    "",
                                    arrayOf(),
                                ),
                        )
                    val response by rememberSaveable { state }

                    val (reservation, setReservation) =
                        rememberSaveable {
                            mutableStateOf<GetReservationResponse?>(
                                null,
                            )
                        }
                    val (sceneObject, setSceneObject) =
                        rememberSaveable {
                            mutableStateOf<SceneObject?>(
                                null,
                            )
                        }

                    if (response.floors.isNotEmpty()) {
                        AITAViewerSurface(response.floors[0]) { selectedSceneObject ->
                            setSceneObject(selectedSceneObject)
                            GlobalScope.launch {
                                setReservation(
                                    ApiClient.instance.fetchReservations(
                                        selectedSceneObject.id,
                                    ),
                                )
                            }
                        }
                    }
                    if (reservation !== null) {
                        ReservationsScreen(sceneObject, reservation, setReservation)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ApiClient.instance.fetchFloorData()
    }
}

@ExperimentalMaterial3Api
@Composable
fun ReservationsScreen(
    sceneObject: SceneObject?,
    reservation: GetReservationResponse?,
    setReservation: (reservation: GetReservationResponse?) -> Unit,
) {
    var showCreateScreen by rememberSaveable { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Reservations",
                    fontSize = 24.sp,
                    modifier =
                        Modifier
                            .align(Alignment.CenterVertically)
                            .padding(5.dp, 5.dp),
                )
                Spacer(Modifier.weight(1f))
                Button(onClick = { showCreateScreen = true }) {
                    Text(text = "+", fontSize = 18.sp)
                }
                Button(onClick = { setReservation(null) }) {
                    Text(text = "×", fontSize = 18.sp)
                }
            }
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
            ) {
                reservation?.reservations?.map {
                    Row(modifier = Modifier.padding(5.dp).fillMaxWidth()) {
                        Surface(modifier = Modifier.width(24.dp).height(24.dp)) {
                            if (it.user.iconImagePath !== null) {
                                AsyncImage(
                                    model = "http://192.168.11.3:3001/${it.user.iconImagePath}",
                                    contentDescription = null,
                                )
                            }
                        }
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(it.user.name)
                            Text("${it.startTimestamp} ~ ${it.endTimestamp}")
                        }
                    }
                }
            }
        }
        if (showCreateScreen) {
            CreateReservationScreen(
                sceneObject!!.id,
                onClickCancel = { showCreateScreen = !showCreateScreen },
                onCreated = {
                    showCreateScreen = false
                    GlobalScope.launch {
                        setReservation(
                            ApiClient.instance.fetchReservations(
                                sceneObject.id,
                            ),
                        )
                    }
                },
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun CreateReservationScreen(
    sheetId: String,
    onClickCancel: () -> Unit,
    onCreated: () -> Unit,
) {
    val calendar = Calendar.getInstance()
    val initial =
        String.format(
            "%04d-%02d-%02d %02d:%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            0,
            0,
        )
    var startDateString by rememberSaveable { mutableStateOf(initial) }
    var endDateString by rememberSaveable { mutableStateOf(initial) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Create Reservation",
                    fontSize = 24.sp,
                    modifier =
                        Modifier
                            .align(Alignment.CenterVertically)
                            .padding(5.dp, 5.dp),
                )
                Spacer(Modifier.weight(1f))
                Button(onClick = onClickCancel) {
                    Text(text = "×", fontSize = 18.sp)
                }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                DateTimePicker(title = "Start Timestamp", onChange = { dateString ->
                    Log.d("DEBUG", "Start : " + dateString)
                    startDateString = dateString
                })
                DateTimePicker(title = "End Timestamp", onChange = { dateString ->
                    Log.d("DEBUG", "End : " + dateString)
                    endDateString = dateString
                })
                Button(onClick = {
                    GlobalScope.launch {
                        val newReservation =
                            ApiClient.instance.createReservation(
                                sheetId,
                                startDateString,
                                endDateString,
                            )
                        if (newReservation !== null) {
                            onCreated()
                        }
                    }
                }, modifier = Modifier.fillMaxWidth().padding(5.dp)) { Text(text = "SAVE") }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun DateTimePicker(
    title: String,
    onChange: (dateString: String) -> Unit,
) {
    val initialCalendar = Calendar.getInstance()
    val dateState = rememberDatePickerState(initialCalendar.timeInMillis)
    val timeState = rememberTimePickerState()
    var showDate by rememberSaveable { mutableStateOf(false) }
    var showTime by rememberSaveable { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(5.dp),
    ) {
        Text(text = title, fontSize = 18.sp)
        Surface(modifier = Modifier.padding(5.dp, 0.dp, 0.dp, 0.dp)) {
            Text(
                text = "Date",
                fontSize = 12.sp,
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(5.dp, 0.dp, 0.dp, 0.dp),
        ) {
            if (dateState.selectedDateMillis !== null) {
                calendar.apply {
                    timeInMillis = dateState.selectedDateMillis!!
                }
                Text(
                    text =
                        String.format(
                            "%04d-%02d-%02d",
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH),
                        ),
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Button({ showDate = !showDate }) {
                Icon(imageVector = Icons.Outlined.Edit, "Edit")
            }
        }
        if (showDate) {
            DatePickerDialog(
                onDismissRequest = { showDate = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDate = false
                        calendar.timeInMillis = dateState.selectedDateMillis!!
                        onChange(
                            String.format(
                                "%04d-%02d-%02d %02d:%02d",
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                timeState.hour,
                                timeState.minute,
                            ),
                        )
                    }) {
                        Text(stringResource(android.R.string.ok))
                    }
                },
            ) { DatePicker(dateState) }
        }
        Surface(modifier = Modifier.padding(5.dp, 0.dp, 0.dp, 0.dp)) {
            Text(
                text = "Time",
                fontSize = 12.sp,
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(5.dp, 0.dp, 0.dp, 0.dp),
        ) {
            Text(text = String.format("%02d:%02d", timeState.hour, timeState.minute))
            Spacer(modifier = Modifier.weight(1f))
            Button({ showTime = !showTime }) {
                Icon(imageVector = Icons.Outlined.Edit, "Edit")
            }
        }
        if (showTime) {
            TimePickerDialog(onDismissRequest = {
                showTime = false
            }, confirmButton = {
                TextButton(
                    onClick = {
                        showTime = false
                        calendar.timeInMillis = dateState.selectedDateMillis!!
                        onChange(
                            String.format(
                                "%04d-%02d-%02d %02d:%02d",
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                timeState.hour,
                                timeState.minute,
                            ),
                        )
                    },
                ) { Text(stringResource(R.string.ok)) }
            }) { TimePicker(timeState) }
        }
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true, widthDp = 320)
@Composable
fun ReservationsScreenPreview() {
    AITA_AndroidTheme {
        ReservationsScreen(sceneObject = null, reservation = null) {
        }
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true, widthDp = 320)
@Composable
fun CreateReservationsScreenPreview() {
    AITA_AndroidTheme {
        CreateReservationScreen(sheetId = "", onClickCancel = {}, onCreated = {})
    }
}
