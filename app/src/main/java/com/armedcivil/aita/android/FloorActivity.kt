package com.armedcivil.aita.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.armedcivil.aita.android.data.Floor
import com.armedcivil.aita.android.http_client.ApiClient
import com.armedcivil.aita.android.http_client.response.FloorResponse
import com.armedcivil.aita.android.ui.theme.AITA_AndroidTheme
import com.armedcivil.aita.android.view.AITAViewerSurface

class FloorActivity : ComponentActivity() {
    companion object {
        const val SCENE_OBJECT = "SCENE_OBJECT"
    }

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
                    var selectedFloor by rememberSaveable { mutableStateOf<Floor?>(null) }

                    if (response.floors.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (selectedFloor != null) {
                                AITAViewerSurface(selectedFloor!!) { selectedSceneObject ->
                                    val intent =
                                        Intent(this@FloorActivity, ReservationActivity::class.java)
                                    intent.putExtra(SCENE_OBJECT, selectedSceneObject)
                                    startActivity(intent)
                                }
                            }
                            Column(modifier = Modifier.align(Alignment.TopStart)) {
                                var expanded by rememberSaveable { mutableStateOf(false) }
                                Button(
                                    onClick = { expanded = true },
                                    modifier = Modifier.width(160.dp),
                                ) {
                                    if (selectedFloor != null) {
                                        Text(
                                            selectedFloor!!.label,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    } else {
                                        Text("Select Floor")
                                    }
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                ) {
                                    response.floors.forEach {
                                        DropdownMenuItem(text = { Text(it.label) }, onClick = {
                                            expanded = false
                                            selectedFloor = it
                                        })
                                    }
                                }
                            }
                        }
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
