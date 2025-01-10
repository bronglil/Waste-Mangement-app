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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

data class ErrorResponse(
    val path: String = "",
    val error: String = "",
    val message: String = "",
    val timestamp: String = "",
    val status: Int = 0
)

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
        data class ApiError(
            val status: Int,
            val error: String,
            val message: String
        ) : Error()
        data class NetworkError(val message: String) : Error()
    }
}

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignUpScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
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

                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
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

                TextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("Contact Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                Text(
                    text = "Contact number should be valid start with country code +33",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                Text(
                    text = "Password requirements:\n" +
                            "• Minimum 8 characters\n" +
                            "• At least one uppercase letter\n" +
                            "• At least one lowercase letter\n" +
                            "• At least one number\n" +
                            "• At least one special character (@#$%^&+=!)\n" +
                            "• No spaces allowed",
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

                LaunchedEffect(signUpState) {
                    when (signUpState) {
                        is SignUpState.Success -> {
                            val successState = signUpState as SignUpState.Success
                            showToastAtTop(context, "Sign up successful!")
                            context.startActivity(Intent(context, LoginActivity::class.java))
                            (context as? ComponentActivity)?.finish()
                        }
                        is SignUpState.Error.ValidationError -> {
                            val errors = (signUpState as SignUpState.Error.ValidationError).errors
                            errors.forEach { (field, message) ->
                                showToastAtTop(context, "$field: $message")
                            }
                        }
                        is SignUpState.Error.ApiError -> {
                            val apiError = signUpState as SignUpState.Error.ApiError
                            showToastAtTop(context, apiError.message)
                        }
                        is SignUpState.Error.NetworkError -> {
                            showToastAtTop(context, (signUpState as SignUpState.Error.NetworkError).message)
                        }
                        else -> { /* Handle other states if needed */ }
                    }
                }
            }
        }
    }
}

fun validateInputs(
    firstName: String,
    lastName: String,
    contactNumber: String,
    email: String,
    password: String,
    context: Context
): Boolean {
    // Check if any field is empty
    if (firstName.isBlank() || lastName.isBlank() || contactNumber.isBlank() || email.isBlank() || password.isBlank()) {
        showToastAtTop(context, "All fields are required!")
        return false
    }

    // Validate contact number format (should start with +33)
    if (!contactNumber.startsWith("+33") || !android.util.Patterns.PHONE.matcher(contactNumber).matches()) {
        showToastAtTop(context, "Contact number must start with +33 and be valid!")
        return false
    }

    // Validate email
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        showToastAtTop(context, "Invalid email address!")
        return false
    }

    // Password validation
    val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$".toRegex()
    if (!password.matches(passwordPattern)) {
        when {
            password.length < 8 -> {
                showToastAtTop(context, "Password must be at least 8 characters long!")
            }
            !password.contains(Regex("[A-Z]")) -> {
                showToastAtTop(context, "Password must contain at least one uppercase letter!")
            }
            !password.contains(Regex("[a-z]")) -> {
                showToastAtTop(context, "Password must contain at least one lowercase letter!")
            }
            !password.contains(Regex("[0-9]")) -> {
                showToastAtTop(context, "Password must contain at least one number!")
            }
            !password.contains(Regex("[@#$%^&+=!]")) -> {
                showToastAtTop(context, "Password must contain at least one special character (@#$%^&+=)!")
            }
            password.contains(" ") -> {
                showToastAtTop(context, "Password must not contain spaces!")
            }
            else -> {
                showToastAtTop(context, "Password must meet all requirements!")
            }
        }
        return false
    }

    return true
}

private fun signUpUser(
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
                        onStateChange(SignUpState.Error.ValidationError(response.errors))
                    }
                    else -> {
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
                when (e) {
                    is retrofit2.HttpException -> {
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            val gson = com.google.gson.Gson()
                            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)

                            onStateChange(SignUpState.Error.ApiError(
                                status = errorResponse.status,
                                error = errorResponse.error,
                                message = errorResponse.message
                            ))
                        } catch (parseError: Exception) {
                            val errorMessage = when (e.code()) {
                                400 -> "Invalid request"
                                401 -> "Unauthorized"
                                403 -> "Forbidden"
                                404 -> "Not found"
                                500 -> "Server error"
                                else -> "Network error: ${e.code()}"
                            }
                            onStateChange(SignUpState.Error.NetworkError(errorMessage))
                        }
                    }
                    is java.net.UnknownHostException -> {
                        onStateChange(SignUpState.Error.NetworkError("No internet connection"))
                    }
                    is java.net.SocketTimeoutException -> {
                        onStateChange(SignUpState.Error.NetworkError("Connection timed out"))
                    }
                    else -> {
                        onStateChange(SignUpState.Error.NetworkError("An unexpected error occurred: ${e.message}"))
                    }
                }
            }
        }
    }
}