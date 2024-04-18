package com.armedcivil.aita.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.armedcivil.aita.android.ui.theme.AITA_AndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AITA_AndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Login()
                    }
                }
            }
        }
    }
}

@Composable
fun Login() {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Logo",
            modifier = Modifier.width(100.dp)
        )
        Text(text = "Login", fontSize = 24.sp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
        ) {
            Text(text = "Email")
            TextField(
                value = email,
                onValueChange = { newValue -> email = newValue },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
        ) {
            Text(text = "Password")
            TextField(
                value = password,
                onValueChange = { newValue -> password = newValue },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Surface(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "LOGIN", fontSize = 24.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }

}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun Preview() {
    AITA_AndroidTheme {
        Login()
    }
}