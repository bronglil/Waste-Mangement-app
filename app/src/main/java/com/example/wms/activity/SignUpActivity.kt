package com.example.wms.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wms.data.network.RetrofitInstance
import com.example.wms.model.SignUpRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.wms.utils.ToastUtils.showToastAtTop

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignUpScreen()
        }
    }
}

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(
        val firstName: String,
        val lastName: String,
        val email: String,
        val token: String
    ) : SignUpState()
    sealed class Error : SignUpState() {
        data class ValidationError(val errors: Map<String, String>) : Error()
        data class NetworkError(val message: String) : Error()
        data class UnknownError(val message: String) : Error()
    }
}

@Composable
fun SignUpScreen() {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signUpState by remember { mutableStateOf<SignUpState>(SignUpState.Idle) }
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
                InputField(value = firstName, label = "First Name") { firstName = it }


                InputField(value = lastName, label = "Last Name") { lastName = it }


                InputField(value = contactNumber, label = "Contact Number", keyboardType = KeyboardType.Phone) { contactNumber = it }
                Text(
                    text = "Contact number should be valid start with country code +33",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                InputField(value = email, label = "Email", keyboardType = KeyboardType.Email) { email = it }


                InputField(value = password, label = "Password", keyboardType = KeyboardType.Password) { password = it }
                Text(
                    text = "Password must be greater than 8 digit long include at least one digit, one lowercase letter, one uppercase letter, one special character, and no whitespace.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (validateInputs(firstName, lastName, contactNumber, email, password, context)) {
                            signUpUser(
                                request = SignUpRequest(
                                    firstName = firstName,
                                    lastName = lastName,
                                    contactNumber = contactNumber,
                                    email = email,
                                    password = password
                                ),
                                onStateChange = { state -> signUpState = state },
                                context = context
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Sign Up", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Already have an account? Go to Login",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                        .clickable {
                            context.startActivity(Intent(context, LoginActivity::class.java))
                        }
                )

                // Handle different states
                LaunchedEffect(signUpState) {
                    when (signUpState) {
                        is SignUpState.Success -> {
                            val successState = signUpState as SignUpState.Success
                            showToastAtTop(context, "Sign up successful!")
                            context.startActivity(Intent(context, LoginActivity::class.java))
                        }
                        is SignUpState.Error.ValidationError -> {
                            val errors = (signUpState as SignUpState.Error.ValidationError).errors
                            errors.forEach { (field, message) ->
                                showToastAtTop(context, "$field: $message")
                            }
                        }
                        is SignUpState.Error.NetworkError -> {
                            showToastAtTop(context, (signUpState as SignUpState.Error.NetworkError).message)
                        }
                        is SignUpState.Error.UnknownError -> {
                            showToastAtTop(context, (signUpState as SignUpState.Error.UnknownError).message)
                        }
                        else -> { /* Handle other states if needed */ }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    value: String,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = Color.Gray,
            containerColor = Color.Transparent
        )
    )
}

fun validateInputs(
    firstName: String,
    lastName: String,
    contactNumber: String,
    email: String,
    password: String,
    context: Context
): Boolean {
    if (firstName.isBlank() || lastName.isBlank() || contactNumber.isBlank() || email.isBlank() || password.isBlank()) {
        showToastAtTop(context, "All fields are required!")
        return false
    }
    if (!android.util.Patterns.PHONE.matcher(contactNumber).matches()) {
        showToastAtTop(context, "Invalid contact number!")
        return false
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        showToastAtTop(context, "Invalid email address!")
        return false
    }
    if (password.length < 8) {
        showToastAtTop(context, "Password must be at least 8 characters long!")
        return false
    }
    return true
}
fun signUpUser(
    request: SignUpRequest,
    onStateChange: (SignUpState) -> Unit,
    context: Context
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            onStateChange(SignUpState.Loading)

            val staticRequest = request.copy(
                userRole = "DRIVER",
                userStatus = "PENDING"
            )

            val response = RetrofitInstance.api.signUpAuthSuspend(staticRequest)

            withContext(Dispatchers.Main) {
                when {
                    response.errors != null -> {
                        // Handle validation errors
                        onStateChange(SignUpState.Error.ValidationError(response.errors))
                    }

                    else -> {
                        // Handle success case
                        onStateChange(
                            SignUpState.Success(
                                firstName = response.firstName ?: "",
                                lastName = response.lastName ?: "",
                                email = response.email ?: "",
                                token = response.token
                            )
                        )
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
                            400 -> "Invalid request"
                            401 -> "Unauthorized"
                            403 -> "Forbidden"
                            404 -> "Not found"
                            500 -> "Server error"
                            else -> "Network error: ${e.code()}"
                        }
                    }
                    else -> "An unexpected error occurred: ${e.message}"
                }
                onStateChange(SignUpState.Error.NetworkError(errorMessage))
            }
        }
    }
}


