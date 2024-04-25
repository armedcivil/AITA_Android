package com.armedcivil.aita.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.armedcivil.aita.android.http_client.ApiClient
import com.armedcivil.aita.android.http_client.response.FloorResponse
import com.armedcivil.aita.android.ui.theme.AITA_AndroidTheme
import com.armedcivil.aita.android.view.AITAViewerSurface

class FloorActivity : ComponentActivity() {
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
                    if (response.floors.isNotEmpty()) {
                        AITAViewerSurface(response.floors[0])
//                        AITAViewer(response.floors[0])
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
