package com.example.kelvinma.activitytracker.ui.analytics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ExportDialog(
    isVisible: Boolean,
    isExporting: Boolean,
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    var emailText by remember { mutableStateOf("") }
    var isValidEmail by remember { mutableStateOf(false) }

    // Simple email validation
    fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    if (isVisible) {
        AlertDialog(
            onDismissRequest = { if (!isExporting) onDismiss() },
            title = {
                Text(
                    text = "Export Database",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter email address to send SQLite database export:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = { 
                            emailText = it
                            isValidEmail = validateEmail(it)
                        },
                        label = { Text("Email Address") },
                        placeholder = { Text("example@email.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !isExporting,
                        isError = emailText.isNotBlank() && !isValidEmail,
                        supportingText = if (emailText.isNotBlank() && !isValidEmail) {
                            { Text("Please enter a valid email address") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isExporting) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Preparing export...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onExport(emailText) },
                    enabled = isValidEmail && !isExporting
                ) {
                    Text("Export & Send")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isExporting
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}