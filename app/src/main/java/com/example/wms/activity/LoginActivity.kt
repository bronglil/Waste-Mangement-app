package com.example.wms.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wms.api.LoginResponse
import com.example.wms.data.network.RetrofitInstance
import com.example.wms.model.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.wms.utils.ToastUtils.showToastAtTop


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginScreen()
        }
    }
}
// First define the LoginState to handle different states
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(
        val userId: Int,
        val firstName: String,
        val token: String
    ) : LoginState()
    sealed class Error : LoginState() {
        data class ValidationError(val errors: Map<String, String>) : Error()
        data class NetworkError(val message: String) : Error()
        data class UnknownError(val message: String) : Error()
    }
}

// Update the LoginResponse to handle validation errors
data class LoginResponse(
    val userId: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val userRole: String = "",
    val token: String = "",
    val message: String? = null,
    val errors: Map<String, String>? = null
)

@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginState by remember { mutableStateOf<LoginState>(LoginState.Idle) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                InputField(
                    value = email,
                    label = "Email",
                    keyboardType = KeyboardType.Email
                ) { email = it }

                InputField(
                    value = password,
                    label = "Password",
                    keyboardType = KeyboardType.Password
                ) { password = it }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (validateInputs(email, password, context)) {
                            loginUser(
                                LoginRequest(email, password),
                                onStateChange = { state -> loginState = state },
                                context = context
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Login", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You have no Account? Go to SignUp",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                        .clickable {
                            context.startActivity(Intent(context, SignUpActivity::class.java))
                        }
                )

                // Handle different states
                LaunchedEffect(loginState) {
                    when (loginState) {
                        is LoginState.Success -> {
                            val successState = loginState as LoginState.Success
                            showToastAtTop(context, "Bonjour ${successState.firstName}!")
                            saveToken(successState.token, context)
                            context.startActivity(Intent(context, MainActivity::class.java))
                        }
                        is LoginState.Error.ValidationError -> {
                            val errors = (loginState as LoginState.Error.ValidationError).errors
                            errors.forEach { (field, message) ->
                                showToastAtTop(context, "$field: $message")
                            }
                        }
                        is LoginState.Error.NetworkError -> {
                            showToastAtTop(context, (loginState as LoginState.Error.NetworkError).message)
                        }
                        is LoginState.Error.UnknownError -> {
                            showToastAtTop(context, (loginState as LoginState.Error.UnknownError).message)
                        }
                        else -> { /* Handle other states if needed */ }
                    }
                }
            }
        }
    }
}

fun validateInputs(
    email: String,
    password: String,
    context: Context
): Boolean {
    return when {
        email.isBlank() -> false.also {
            showToastAtTop(context, "Email is required")
        }
        password.isBlank() -> false.also {
            showToastAtTop(context, "Password is required")
        }
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> false.also {
            showToastAtTop(context, "Please enter a valid email address")
        }
        password.length < 8 -> false.also {
            showToastAtTop(context, "Password must be at least 8 characters")
        }
        else -> true
    }
}

private fun loginUser(
    request: LoginRequest,
    onStateChange: (LoginState) -> Unit,
    context: Context
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            onStateChange(LoginState.Loading)

            val response = RetrofitInstance.api.login(request)

            withContext(Dispatchers.Main) {
                when {
                    response.errors != null -> {
                        onStateChange(LoginState.Error.ValidationError(response.errors))
                    }
                    response.token.isNotEmpty() -> {
                        saveUserDetails(response, context)
                        onStateChange(LoginState.Success(
                            userId = response.userId,
                            firstName = response.firstName,
                            token = response.token
                        ))

                    }
                    else -> {
                        onStateChange(LoginState.Error.UnknownError(
                            response.message ?: "Unknown error occurred"
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "No internet connection"
                    is java.net.SocketTimeoutException -> "Connection timed out"
                    is retrofit2.HttpException -> {
                        when (e.code()) {
                            400 -> "Invalid credentials"
                            401 -> "Invalid email or password"
                            403 -> "Account locked"
                            404 -> "Account not found"
                            500 -> "Server error"
                            else -> "Network error: ${e.code()}"
                        }
                    }
                    else -> "An unexpected error occurred: ${e.message}"
                }
                onStateChange(LoginState.Error.NetworkError(errorMessage))
            }
        }
    }
}

private fun saveToken(token: String, context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("auth_token", token).apply()
}


private fun saveUserDetails(response: LoginResponse, context: Context) {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putInt("userId", response.userId)
        putString("firstName", response.firstName)
        putString("lastName", response.lastName)
        putString("email", response.email)
        putString("token", response.token)
        apply()
    }
}
