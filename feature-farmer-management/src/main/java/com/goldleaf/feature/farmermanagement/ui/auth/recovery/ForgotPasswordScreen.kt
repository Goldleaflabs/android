package com.goldleaf.feature.farmermanagement.ui.auth.recovery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RecoveryViewModel = hiltViewModel()
) {
    val recoveryState by viewModel.recoveryState.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val otp by viewModel.otp.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()

    // Determine which step to show
    val currentStep = when (recoveryState) {
        is RecoveryState.OTPSent -> 2
        is RecoveryState.OTPVerified -> 3
        is RecoveryState.PasswordResetSuccess -> 4
        else -> 1
    }

    LaunchedEffect(recoveryState) {
        if (recoveryState is RecoveryState.PasswordResetSuccess) {
            // Navigate to login after successful reset
            kotlinx.coroutines.delay(2000)
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Recovery") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Indicator
            StepIndicator(currentStep = currentStep, totalSteps = 3)

            Spacer(modifier = Modifier.height(32.dp))

            when (currentStep) {
                1 -> Step1RequestOTP(viewModel, phone, recoveryState)
                2 -> Step2VerifyOTP(viewModel, otp, recoveryState)
                3 -> Step3ResetPassword(viewModel, newPassword, recoveryState)
                4 -> SuccessMessage()
            }
        }
    }
}

@Composable
fun Step1RequestOTP(
    viewModel: RecoveryViewModel,
    phone: String,
    state: RecoveryState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.CallReceived,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Enter Your Phone Number",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "We'll send you a one-time password to reset your account",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { viewModel.onPhoneChange(it) },
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.CallReceived, null) },
            modifier = Modifier.fillMaxWidth()
        )

        if (state is RecoveryState.Error) {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = { viewModel.requestOTP() },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is RecoveryState.Loading
        ) {
            if (state is RecoveryState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Send OTP")
            }
        }
    }
}

@Composable
fun Step2VerifyOTP(
    viewModel: RecoveryViewModel,
    otp: String,
    state: RecoveryState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Sms,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Enter OTP",
            style = MaterialTheme.typography.headlineSmall
        )

        if (state is RecoveryState.OTPSent) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = state.message,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = otp,
            onValueChange = { viewModel.onOTPChange(it) },
            label = { Text("OTP Code") },
            leadingIcon = { Icon(Icons.Default.Password, null) },
            modifier = Modifier.fillMaxWidth()
        )

        if (state is RecoveryState.Error) {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = { viewModel.verifyOTP() },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is RecoveryState.Loading
        ) {
            if (state is RecoveryState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Verify OTP")
            }
        }

        TextButton(onClick = { viewModel.requestOTP() }) {
            Text("Resend OTP")
        }
    }
}

@Composable
fun Step3ResetPassword(
    viewModel: RecoveryViewModel,
    newPassword: String,
    state: RecoveryState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Set New Password",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = newPassword,
            onValueChange = { viewModel.onNewPasswordChange(it) },
            label = { Text("New Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (state is RecoveryState.Error) {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = { viewModel.resetPassword() },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is RecoveryState.Loading
        ) {
            if (state is RecoveryState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Reset Password")
            }
        }
    }
}

@Composable
fun SuccessMessage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Password Reset Successful!",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Redirecting to login...",
            style = MaterialTheme.typography.bodyMedium
        )

        CircularProgressIndicator()
    }
}

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { step ->
            val stepNumber = step + 1
            val isActive = stepNumber <= currentStep

            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .height(4.dp)
                    .weight(1f)
            ) {}
        }
    }
}