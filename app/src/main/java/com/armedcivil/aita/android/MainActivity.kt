package com.armedcivil.aita.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.armedcivil.aita.android.http_client.ApiClient
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
                        Login(handleClickLogin)
                    }
                }
            }
        }
    }

    private val handleClickLogin = { email: String, password: String ->
        ApiClient.instance.signin(email, password) {
            if (it) {
                val intent = Intent(this, FloorActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}

@Composable
fun Login(handleClickLogin: (email: String, password: String) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val formVisible = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Logo",
            modifier = Modifier.width(100.dp),
        )
        AnimatedVisibility(
            visibleState = formVisible,
            enter = expandVertically(
                initialHeight = { 0 },
                animationSpec = tween(durationMillis = 400, delayMillis = 2500)
            )
                    + fadeIn(
                initialAlpha = 0f,
                animationSpec = tween(durationMillis = 800, delayMillis = 2500)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Login", fontSize = 24.sp, modifier = Modifier.padding(top = 10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp, start = 4.dp, end = 4.dp, bottom = 4.dp),
                ) {
                    TextField(
                        value = email,
                        onValueChange = { newValue -> email = newValue },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Email") },
                        singleLine = true,
                    )
                }
                Column(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 4.dp),
                ) {
                    TextField(
                        value = password,
                        onValueChange = { newValue -> password = newValue },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            val image =
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description =
                                if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, description)
                            }
                        }
                    )
                }
                Surface(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                    Button(
                        onClick = { handleClickLogin(email, password) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "LOGIN",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun Preview() {
    AITA_AndroidTheme {
        Login(handleClickLogin = { email, password -> })
    }
}
