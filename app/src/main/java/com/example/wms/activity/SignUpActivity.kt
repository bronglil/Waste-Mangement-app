package com.example.wms.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignUpScreen()
        }
    }
}

@Composable
fun SignUpScreen() {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
                InputField(value = mobile, label = "Contact Number", keyboardType = KeyboardType.Phone) { mobile = it }
                InputField(value = email, label = "Email", keyboardType = KeyboardType.Email) { email = it }
                InputField(value = password, label = "Password", keyboardType = KeyboardType.Password) { password = it }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (validateInputs(firstName, lastName, mobile, email, password, context)) {
                            signUpUser(
                                request = SignUpRequest(
                                    firstName = firstName,
                                    lastName = lastName,
                                    mobile = mobile,
                                    email = email,
                                    password = password
                                ),
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
    mobile: String,
    email: String,
    password: String,
    context: android.content.Context
): Boolean {
    if (firstName.isBlank() || lastName.isBlank() || mobile.isBlank() || email.isBlank() || password.isBlank()) {
        Toast.makeText(context, "All fields are required!", Toast.LENGTH_LONG).show()
        return false
    }
    if (!android.util.Patterns.PHONE.matcher(mobile).matches()) {
        Toast.makeText(context, "Invalid contact number!", Toast.LENGTH_LONG).show()
        return false
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        Toast.makeText(context, "Invalid email address!", Toast.LENGTH_LONG).show()
        return false
    }
    if (password.length < 8) {
        Toast.makeText(context, "Password must be at least 8 characters long!", Toast.LENGTH_LONG).show()
        return false
    }
    return true
}

fun signUpUser(request: SignUpRequest, context: android.content.Context) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.signUpSuspend(request)
            withContext(Dispatchers.Main) {
                if (response.success) {
                    Toast.makeText(
                        context,
                        "Sign Up Successful: ${response.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                } else {
                    Toast.makeText(
                        context,
                        "Sign Up Failed: ${response.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Error: ${e.localizedMessage ?: "Something went wrong"}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("SignUpError", "Exception occurred", e)
            }
        }
    }
}
