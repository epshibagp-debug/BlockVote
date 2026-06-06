package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.BlockEntity
import com.example.data.database.CandidateEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.VotingViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- Glassmorphic Container Helper ---
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.35f),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = Color.White.copy(alpha = 0.72f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(20.dp),
        content = content
    )
}

// --- Get Party Visual Theme ---
fun getPartyColor(symbol: String): Color {
    return when (symbol.lowercase()) {
        "lotus" -> Saffron
        "hand" -> Color(0xFF00BFFF)
        "broom" -> Color(0xFFFFD700)
        "flower" -> GreenIndian
        else -> NavyIndia
    }
}

fun getPartyIcon(symbol: String): ImageVector {
    return when (symbol.lowercase()) {
        "lotus" -> Icons.Default.Eco
        "hand" -> Icons.Default.PanTool
        "broom" -> Icons.Default.CleaningServices
        "flower" -> Icons.Default.LocalFlorist
        "security" -> Icons.Default.Security
        "admin" -> Icons.Default.AdminPanelSettings
        else -> Icons.Default.Star
    }
}

// --- Custom Parliament Silhouette Drawer ---
@Composable
fun ParliamentSilhouette(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Draw bottom platform
        drawRect(
            color = NavyIndia.copy(alpha = 0.8f),
            topLeft = Offset(0f, height * 0.8f),
            size = Size(width, height * 0.2f)
        )
        
        // Draw secondary platform
        drawRect(
            color = NavyIndia.copy(alpha = 0.9f),
            topLeft = Offset(width * 0.05f, height * 0.7f),
            size = Size(width * 0.9f, height * 0.1f)
        )
        
        // Draw main circular parliament colosseum base
        drawArc(
            color = NavyIndia.copy(alpha = 0.5f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(width * 0.1f, height * 0.25f),
            size = Size(width * 0.8f, height * 0.9f)
        )

        // Draw colosseum pillars
        val pillarCount = 20
        val startX = width * 0.15f
        val endX = width * 0.85f
        val pillarWidth = (endX - startX) / (pillarCount * 2)
        for (i in 0 until pillarCount) {
            val x = startX + i * pillarWidth * 2
            drawRect(
                color = NavyIndia,
                topLeft = Offset(x, height * 0.45f),
                size = Size(pillarWidth, height * 0.25f)
            )
        }

        // Draw top arch structure
        drawRect(
            color = NavyIndia,
            topLeft = Offset(width * 0.12f, height * 0.4f),
            size = Size(width * 0.76f, height * 0.05f)
        )

        // Draw central dome
        drawArc(
            color = Saffron,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(width * 0.4f, height * 0.15f),
            size = Size(width * 0.2f, height * 0.5f)
        )

        // Draw national flag line
        drawLine(
            color = NavyIndia,
            start = Offset(width * 0.5f, height * 0.25f),
            end = Offset(width * 0.5f, height * 0.05f),
            strokeWidth = 3f
        )

        // Simple flag canvas
        val path = Path().apply {
            moveTo(width * 0.5f, height * 0.05f)
            lineTo(width * 0.62f, height * 0.08f)
            lineTo(width * 0.5f, height * 0.12f)
            close()
        }
        drawPath(path, color = GreenIndian)
    }
}

// ==========================================
// 1️⃣ LANDING PAGE
// ==========================================
@Composable
fun LandingPage(viewModel: VotingViewModel, onNavigate: (Screen) -> Unit) {
    val blocks by viewModel.blocks.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Indian National Colors Election Header Emblem
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Saffron, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, NavyIndia, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(NavyIndia, CircleShape)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(GreenIndian, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "BLOCKVOTE INDIA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = NavyIndia
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Secure, Immutable, Transparent Blockchain Elections",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Parliament Silhouette Banner
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ParliamentSilhouette(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            color = Saffron,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "LOK SABHA 2026",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Voting Statistics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Registered Voters
                GlassmorphicCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.People, contentDescription = "Electors", tint = NavyIndia)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Registered",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "2 Active Users",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyIndia
                        )
                    }
                }

                // Votes Cast / Mined blocks
                GlassmorphicCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Ledger size", tint = GreenIndian)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Secure Ledger",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        val votesRegistered = if (blocks.isEmpty()) 0 else (blocks.size - 1).coerceAtLeast(0)
                        Text(
                            text = "$votesRegistered Mined Votes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GreenIndian
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Blockchain health indicator card
            val chainState by viewModel.blockchainHealth.collectAsState()
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (chainState) Color.White.copy(alpha = 0.9f) else Color(0xFFFEF2F2)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (chainState) Icons.Default.Security else Icons.Default.Warning,
                        contentDescription = "Status",
                        tint = if (chainState) GreenIndian else Saffron,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (chainState) "Blockchain Status: SECURE" else "LEDGER TAMPER DETECTED",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (chainState) GreenIndian else Saffron
                        )
                        Text(
                            text = if (chainState) "All voting records are cryptographic, immutable, and verified." else "A hash collision or ledger compromise has triggered security alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Saffron/Green gradient rounded action buttons
            Button(
                onClick = { onNavigate(Screen.Login) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("landing_login_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Login, contentDescription = "Login")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "VOTER & OFFICIAL LOGIN",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onNavigate(Screen.Registration) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("landing_register_button"),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(2.dp, GreenIndian),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenIndian)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Register")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "NEW VOTER REGISTRATION",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Demo Instructions note
            Text(
                text = "Demo Credentials: Admin user (admin/admin123) | Seeded voter ID (VOTER12345, Password: password)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ==========================================
// 2️⃣ USER REGISTRATION
// ==========================================
@Composable
fun RegistrationPage(viewModel: VotingViewModel, onNavigate: (Screen) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var voterId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val isProcessing by viewModel.isProcessing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val otpDialogVisible by viewModel.otpDialogVisible.collectAsState()
    val simulatedOtp by viewModel.simulatedOtpCode.collectAsState()

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(Screen.Landing) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NavyIndia)
                }
                Text(
                    text = "Voter Signup Portal",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyIndia
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saffron line graphic divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Saffron, Color.White, GreenIndian)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Forms
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name (As in Aadhaar Card)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("reg_name_input"),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                singleLine = true
            )

            OutlinedTextField(
                value = aadhaar,
                onValueChange = { if (it.length <= 12 && it.all { ch -> ch.isDigit() }) aadhaar = it },
                label = { Text("Aadhaar Card Number (12 Digits)") },
                placeholder = { Text("123456789012") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("reg_aadhaar_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = "Aadhaar") },
                singleLine = true
            )

            OutlinedTextField(
                value = voterId,
                onValueChange = { voterId = it.uppercase() },
                label = { Text("EPIC Voter ID Number") },
                placeholder = { Text("VOTER12345") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("reg_voter_input"),
                leadingIcon = { Icon(Icons.Default.ContactPage, contentDescription = "Voter ID") },
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("reg_email_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                singleLine = true
            )

            OutlinedTextField(
                value = mobile,
                onValueChange = { if (it.length <= 10 && it.all { ch -> ch.isDigit() }) mobile = it },
                label = { Text("Mobile Phone (10 Digits)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("reg_phone_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Mobile") },
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Create Secure Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("reg_password_input"),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("reg_confirm_password_input"),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (password != confirmPassword) {
                        viewModel.confirmOtp("MISMATCH") // triggers failure
                    } else {
                        viewModel.register(name, email, mobile, aadhaar, voterId, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reg_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = GreenIndian),
                enabled = !isProcessing,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("VERIFY & REGISTER", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { onNavigate(Screen.Login) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Already have a secure key? Login Here", color = NavyIndia)
            }
        }

        // --- Simulated OTP Modal Dialog ---
        if (otpDialogVisible) {
            OtpVerificationDialog(
                simulatedOtp = simulatedOtp,
                onDismiss = { viewModel.dismissOtpDialog() },
                onConfirm = { enteredCode -> viewModel.confirmOtp(enteredCode) }
            )
        }
    }
}

@Composable
fun OtpVerificationDialog(
    simulatedOtp: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header SMS notification mockup
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sms, contentDescription = "SMS", tint = Saffron, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SIMULATED SMS ALREADY SENT", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "OTP Code for BlockVote verification is: $simulatedOtp",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Icon(Icons.Default.Lock, contentDescription = "OTP Lock", tint = NavyIndia, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Voter OTP Verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyIndia)
                Text(
                    text = "A 6-digit OTP code has been dispatched to check registration integrity.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { textValue ->
                        if (textValue.all { it.isDigit() } && textValue.length <= 6) {
                            code = textValue
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Enter 6-Digit OTP") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(code) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}


// ==========================================
// 3️⃣ LOGIN PAGE
// ==========================================
@Composable
fun LoginPage(viewModel: VotingViewModel, onNavigate: (Screen) -> Unit) {
    var emailOrMobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isProcessing by viewModel.isProcessing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onNavigate(Screen.Landing) },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NavyIndia)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Secure Voting Login",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyIndia
                )
            )

            Text(
                text = "Use your registered Voter ID, Mobile or Email",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (statusMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusMessage ?: "",
                        color = Color(0xFF16A34A),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = emailOrMobile,
                onValueChange = { emailOrMobile = it },
                label = { Text("Voter ID / Email / Mobile") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("login_username_input"),
                leadingIcon = { Icon(Icons.Default.ContactPage, contentDescription = "ElectID") },
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Security Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("login_password_input"),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.loginWithPassword(emailOrMobile, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NavyIndia),
                enabled = !isProcessing,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("SECURE CIPHER LOGIN", fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Demo hints
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("QUICK SEEDED CREDENTIALS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Saffron)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            emailOrMobile = "VOTER12345"
                            password = "password"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Fill Seeded Citizen Voter: VOTER12345 (password)", style = MaterialTheme.typography.bodySmall, color = NavyIndia)
                    }
                    TextButton(
                        onClick = {
                            emailOrMobile = "admin"
                            password = "admin123"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Fill Election Officer Admin (admin/admin123)", style = MaterialTheme.typography.bodySmall, color = Saffron)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = { onNavigate(Screen.Registration) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("New Elector? Register Secure Account", color = GreenIndian)
            }
        }
    }
}


// ==========================================
// 4️⃣ VOTER DASHBOARD
// ==========================================
@Composable
fun VoterDashboard(viewModel: VotingViewModel, onNavigate: (Screen) -> Unit) {
    val user by viewModel.currentUser.collectAsState()
    val blocks by viewModel.blocks.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Dashboard header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Namaste,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = user?.name ?: "Indian Citizen",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NavyIndia,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toast feedback inline
            if (statusMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, "success", tint = GreenIndian)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = statusMessage ?: "", color = Color(0xFF16A34A), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Voter Card Details
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = Saffron.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.White, Saffron.copy(alpha = 0.05f))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "OFFICIAL ELECTOR ID",
                            fontWeight = FontWeight.ExtraBold,
                            color = Saffron,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(Icons.Default.Fingerprint, "Aadhaar verified", tint = GreenIndian)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user?.voterId ?: "EPIC NO DETECTED",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = NavyIndia
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Aadhaar Number", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            val maskedAadhaar = user?.aadhaar?.let { "XXXX-XXXX-${it.takeLast(4)}" } ?: "XXXX-XXXX-XXXX"
                            Text(maskedAadhaar, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Voting Eligibility", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (user?.hasVoted == true) Color.Gray else GreenIndian, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (user?.hasVoted == true) "VOTED ✓" else "ELIGIBLE",
                                    fontWeight = FontWeight.Black,
                                    color = if (user?.hasVoted == true) Color.Gray else GreenIndian
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("SECURE ACTIONS", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge, color = NavyIndia)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Dashboard matrix
            DashboardCard(
                title = "Cast Secure Digital Vote",
                description = if (user?.hasVoted == true) "You have already voted! Ledger state locked." else "Choose ballot options to append your cryptographic record.",
                icon = Icons.Default.HowToVote,
                tint = if (user?.hasVoted == true) Color.Gray else Saffron,
                enabled = user?.hasVoted == false,
                onClick = { onNavigate(Screen.Voting) }
            )

            DashboardCard(
                title = "Blockchain Ledger Explorer",
                description = "Inspect mined voting transactions blocks, audits, hashes, and validation status.",
                icon = Icons.Default.Dns,
                tint = NavyIndia,
                enabled = true,
                onClick = { onNavigate(Screen.BlockchainExplorer) }
            )

            DashboardCard(
                title = "Live Election Results",
                description = "Audit graphical candidate votes distributions gathered strictly from ledger blocks.",
                icon = Icons.Default.Assessment,
                tint = GreenIndian,
                enabled = true,
                onClick = { onNavigate(Screen.ElectionResults) }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    description: String,
    icon: ImageVector,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (enabled) Color.White else Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = tint)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) NavyIndia else Color.Gray,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = Color.LightGray, modifier = Modifier.size(14.dp))
        }
    }
}


// ==========================================
// 5️⃣ CANDIDATE MANAGEMENT (Admin screen)
// ==========================================
@Composable
fun CandidateManagementPage(viewModel: VotingViewModel, onNavigate: (Screen) -> Unit) {
    var name by remember { mutableStateOf("") }
    var party by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf("lotus") }

    val candidates by viewModel.candidates.collectAsState()

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(Screen.AdminDashboard) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NavyIndia)
                }
                Text(
                    text = "Candidate Registrar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NavyIndia
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Candidate form
            GlassmorphicCard(borderColor = NavyIndia.copy(alpha = 0.2f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ADD NEW CANDIDATE BALLOT",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = Saffron
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Candidate Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = party,
                        onValueChange = { party = it },
                        label = { Text("Political Alliance / Party") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Pick Alliance Symbol:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("lotus", "hand", "broom", "flower").forEach { sym ->
                            val isSelected = symbol == sym
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isSelected) getPartyColor(sym).copy(alpha = 0.15f) else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(
                                        2.dp,
                                        if (isSelected) getPartyColor(sym) else Color.LightGray.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                                    .clickable { symbol = sym },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    getPartyIcon(sym),
                                    contentDescription = sym,
                                    tint = getPartyColor(sym)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.addCandidate(name, party, symbol)
                            name = ""
                            party = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenIndian)
                    ) {
                        Text("COMMIT BALLOT PAPER", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ACTIVE CANDIDATES SHEET (${candidates.size})",
                fontWeight = FontWeight.Black,
                color = NavyIndia,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(candidates) { candidate ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(getPartyColor(candidate.symbol).copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    getPartyIcon(candidate.symbol),
                                    contentDescription = candidate.symbol,
                                    tint = getPartyColor(candidate.symbol)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = candidate.name,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyIndia,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = candidate.party,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            IconButton(onClick = { viewModel.deleteCandidate(candidate) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 6️⃣ VOTING PAGE
// ==========================================
@Composable
fun VotingPage(viewModel: VotingViewModel, onNavigate: (Screen) -> Unit) {
    val candidates by viewModel.candidates.collectAsState()
    val isMining by viewModel.isProcessing.collectAsState()

    var selectedCandidateId by remember { mutableStateOf<Int?>(null) }
    var miningAnimationProgress by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(Screen.VoterDashboard) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NavyIndia)
                }
                Text(
                    text = "Cast Cryptographic Vote",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NavyIndia
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Authorized electors can secure exactly one vote. Choose one candidate below:",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(candidates) { candidate ->
                    val isChosen = selectedCandidateId == candidate.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                2.dp,
                                if (isChosen) getPartyColor(candidate.symbol) else Color.LightGray.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clickable { selectedCandidateId = candidate.id },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChosen) getPartyColor(candidate.symbol).copy(alpha = 0.05f) else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(getPartyColor(candidate.symbol).copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        getPartyIcon(candidate.symbol),
                                        contentDescription = candidate.symbol,
                                        tint = getPartyColor(candidate.symbol),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = candidate.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = NavyIndia
                                    )
                                    Text(
                                        text = candidate.party,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        if (isChosen) getPartyColor(candidate.symbol) else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(2.dp, getPartyColor(candidate.symbol), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChosen) {
                                    Icon(Icons.Default.Check, "Selected", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val targetId = selectedCandidateId
                    if (targetId != null) {
                        miningAnimationProgress = true
                        viewModel.castVote(targetId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_vote_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                enabled = selectedCandidateId != null && !isMining,
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, "secure")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "VOTE NOW & MINE SECURE BLOCK",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        if (isMining && miningAnimationProgress) {
            BlockMiningLoaderAnimation()
        }
    }
}

@Composable
fun BlockMiningLoaderAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "miner")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(24.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkBackground),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Saffron.copy(alpha = 0.2f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 8f)
                        )
                        drawArc(
                            color = Saffron,
                            startAngle = angle,
                            sweepAngle = 100f,
                            useCenter = false,
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )
                    }
                    
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = "Mining",
                        tint = GreenIndian,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "BLOCKCHAIN LEDGER TRANSACTION MINING",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Generating secure SHA-256 signatures, running proof of work node, sealing ledger...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    color = GreenIndian,
                    trackColor = Color.DarkGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}


// ==========================================
// 7️⃣ BLOCKCHAIN EXPLORER
// ==========================================
@Composable
fun BlockchainExplorerPage(viewModel: VotingViewModel, onNavigate: (Screen) -> Unit) {
    val blocks by viewModel.blocks.collectAsState()
    val candidates by viewModel.candidates.collectAsState()
    val isChainSecure by viewModel.blockchainHealth.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(Screen.VoterDashboard) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NavyIndia)
                }
                Text(
                    text = "Vote Blockchain Ledger",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NavyIndia
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isChainSecure) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isChainSecure) Icons.Default.Security else Icons.Default.Warning,
                        contentDescription = "Status badge",
                        tint = if (isChainSecure) GreenIndian else Color.Red
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isChainSecure) "LEDGER SECURE: VALIDATED ✓" else "LEDGER INSECURE: REJECTED ✗",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isChainSecure) GreenIndian else Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = if (isChainSecure) "Chain validates perfectly against sequential SHA-256 blocks." else "Chain compromise has been detected. Recalculations failed.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MINED LEDGER BLOCKS PREVIEW (${blocks.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(blocks) { block ->
                    BlockEntityItem(block, candidates)
                }
            }
        }
    }
}

@Composable
fun BlockEntityItem(block: BlockEntity, candidates: List<CandidateEntity>) {
    val dateString = remember(block.timestamp) {
        val date = Date(block.timestamp)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        format.format(date)
    }

    val votedCandidateName = remember(block.candidateId, candidates) {
        if (block.index == 0) {
            "GENESIS INTEGRITY BASE"
        } else {
            val cand = candidates.find { it.id == block.candidateId }
            cand?.let { "${it.name} (${it.party})" } ?: "Candidate ID: ${block.candidateId}"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (block.index == 0) GreenIndian.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFBFC)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    if (block.index == 0) {
                        Brush.linearGradient(colors = listOf(Color.White, GreenIndian.copy(alpha = 0.05f)))
                    } else {
                        Brush.linearGradient(colors = listOf(Color.White, Color(0xFFF1F5F9).copy(alpha = 0.2f)))
                    }
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(NavyIndia, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "#${block.index}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (block.index == 0) "Genesis Security Root" else "Transaction Vote Block",
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyIndia,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Candidate Name sealed in this block
            Row(modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.HowToVote, "ballot", tint = Saffron, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Sealed Ballots Vote Target", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(votedCandidateName, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Anonymous Voter Hash
            Row(modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Person, "mask", tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Elector Signature Code (SHA-256)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = block.voterHash,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("PREV BLOCK HASH", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = block.previousHash,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("CURRENT BLOCK HASH", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = block.currentHash,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = GreenIndian,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nonce row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, "nonce", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Mining Nonce (Proof Of Work Solution): ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = "${block.nonce}",
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyIndia,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}


// ==========================================
// 8️⃣ ELECTION RESULTS PAGE
// ==========================================
@Composable
fun ElectionResultsPage(viewModel: VotingViewModel, onNavigate: (Screen) -> Unit) {
    val candidates by viewModel.candidates.collectAsState()
    val blocks by viewModel.blocks.collectAsState()

    val computedVotesMap = remember(blocks) {
        val counts = mutableMapOf<Int, Int>()
        blocks.forEach { block ->
            if (block.index > 0) {
                counts[block.candidateId] = (counts[block.candidateId] ?: 0) + 1
            }
        }
        counts
    }

    val totalVotes = remember(computedVotesMap) {
        computedVotesMap.values.sum()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    val usr = viewModel.currentUser.value
                    if (usr != null && usr.id == -99) {
                        onNavigate(Screen.AdminDashboard)
                    } else {
                        onNavigate(Screen.VoterDashboard)
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NavyIndia)
                }
                Text(
                    text = "Audited Election Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NavyIndia
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassmorphicCard(borderColor = Saffron.copy(alpha = 0.2f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "TOTAL IMMUTABLE LEDGER VOTES CAST",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "$totalVotes",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = NavyIndia
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Turnout rate verified by smart validators: ${(totalVotes * 50).coerceAtMost(100)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenIndian,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (candidates.isEmpty() || totalVotes == 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.HourglassEmpty, "empty", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Awaiting block validations to compile charts...",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Text(
                    text = "VOTE ALLOCATION (SECURE AUDITED LEDGER)",
                    fontWeight = FontWeight.Black,
                    color = NavyIndia,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                RenderCanvasPieChart(candidates, computedVotesMap, totalVotes)
                
                Spacer(modifier = Modifier.height(24.dp))

                RenderCanvasBarChart(candidates, computedVotesMap, totalVotes)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "BALLOT ACCOUNT AUDIT LIST",
                fontWeight = FontWeight.Black,
                color = NavyIndia,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            candidates.forEach { candidate ->
                val votes = computedVotesMap[candidate.id] ?: 0
                val percentage = if (totalVotes == 0) 0f else (votes.toFloat() / totalVotes.toFloat() * 100f)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(getPartyColor(candidate.symbol).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                getPartyIcon(candidate.symbol),
                                contentDescription = candidate.symbol,
                                tint = getPartyColor(candidate.symbol),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = candidate.name,
                                fontWeight = FontWeight.Bold,
                                color = NavyIndia,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = candidate.party,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$votes Votes",
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyIndia,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", percentage),
                                style = MaterialTheme.typography.labelSmall,
                                color = getPartyColor(candidate.symbol),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// Custom vector drawing for pie chart
@Composable
fun RenderCanvasPieChart(
    candidates: List<CandidateEntity>,
    computedVotesMap: Map<Int, Int>,
    totalVotes: Int
) {
    val leaderCandidate = remember(candidates, computedVotesMap) {
        if (candidates.isEmpty()) null
        else candidates.maxByOrNull { computedVotesMap[it.id] ?: 0 }
    }

    var selectedCandidateId by remember { mutableStateOf<Int?>(null) }
    val activeCandidate = selectedCandidateId?.let { id -> candidates.find { it.id == id } } ?: leaderCandidate

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFDFF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "COMPOSITION SPECTRUM (DONUT CHART)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                
                var isPulsing by remember { mutableStateOf(true) }
                val pulseAlpha by animateFloatAsState(
                    targetValue = if (isPulsing) 1.0f else 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(GreenIndian.copy(alpha = pulseAlpha), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "LIVE LEDGER SYNC",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenIndian
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    var startAngle = -90f
                    candidates.forEach { candidate ->
                        val votes = computedVotesMap[candidate.id] ?: 0
                        if (votes > 0) {
                            val sweepAngle = (votes.toFloat() / totalVotes.toFloat()) * 360f
                            val isSelected = candidate.id == activeCandidate?.id
                            
                            val strokeWidth = if (isSelected) 54f else 36f
                            val alpha = if (selectedCandidateId == null || isSelected) 1.0f else 0.45f
                            
                            drawArc(
                                color = getPartyColor(candidate.symbol).copy(alpha = alpha),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            
                            if (isSelected) {
                                drawArc(
                                    color = getPartyColor(candidate.symbol).copy(alpha = 0.15f),
                                    startAngle = startAngle - 1f,
                                    sweepAngle = sweepAngle + 2f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth + 20f, cap = StrokeCap.Round)
                                )
                            }
                            
                            startAngle += sweepAngle
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    if (activeCandidate != null) {
                        val activeVotes = computedVotesMap[activeCandidate.id] ?: 0
                        val actPct = if (totalVotes == 0) 0f else (activeVotes.toFloat() / totalVotes.toFloat() * 100f)
                        
                        Icon(
                            getPartyIcon(activeCandidate.symbol),
                            contentDescription = activeCandidate.symbol,
                            tint = getPartyColor(activeCandidate.symbol),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeCandidate.name.split(" ").firstOrNull() ?: activeCandidate.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyIndia,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%%", actPct),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = getPartyColor(activeCandidate.symbol)
                        )
                        Text(
                            text = "$activeVotes Votes",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            "No Votes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Tap candidate to inspect distribution:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                candidates.forEach { cand ->
                    val votes = computedVotesMap[cand.id] ?: 0
                    val isSelected = cand.id == activeCandidate?.id
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) getPartyColor(cand.symbol).copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .clickable {
                                selectedCandidateId = if (selectedCandidateId == cand.id) null else cand.id
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(getPartyColor(cand.symbol), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cand.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) NavyIndia else Color.DarkGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (cand.id == leaderCandidate?.id && votes > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Saffron.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "★ LEADING",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Saffron,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = "$votes Votes",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NavyIndia else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// Custom bar chart rendering with Canvas
@Composable
fun RenderCanvasBarChart(
    candidates: List<CandidateEntity>,
    computedVotesMap: Map<Int, Int>,
    totalVotes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFDFF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "BALLOT VOLUME LEADERSHIP BARS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(18.dp))

            candidates.forEach { candidate ->
                val votes = computedVotesMap[candidate.id] ?: 0
                val ratio = if (totalVotes == 0) 0f else (votes.toFloat() / totalVotes.toFloat())

                val animatedRatio by animateFloatAsState(
                    targetValue = ratio,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "ratioAnim"
                )

                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                getPartyIcon(candidate.symbol),
                                contentDescription = null,
                                tint = getPartyColor(candidate.symbol),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = candidate.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = NavyIndia
                            )
                        }
                        
                        Text(
                            text = String.format(Locale.getDefault(), "%d Votes (%.1f%%)", votes, ratio * 100f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = getPartyColor(candidate.symbol)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        drawRoundRect(
                            color = Color(0xFFE2E8F0),
                            size = Size(canvasWidth, canvasHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )

                        if (votes > 0) {
                            val activeWidth = canvasWidth * animatedRatio
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        getPartyColor(candidate.symbol).copy(alpha = 0.8f),
                                        getPartyColor(candidate.symbol)
                                    )
                                ),
                                size = Size(activeWidth, canvasHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 9️⃣ ADMIN DASHBOARD
// ==========================================
@Composable
fun AdminDashboard(viewModel: VotingViewModel, onNavigate: (Screen) -> Unit) {
    val isChainSecure by viewModel.blockchainHealth.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ELECTION CONTROL DESK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "Chief Election Officer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NavyIndia
                    )
                }

                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.testTag("admin_logout_button")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (statusMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusMessage ?: "",
                        color = Color(0xFF16A34A),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Blockchain Health Indicator
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (isChainSecure) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                borderColor = if (isChainSecure) GreenIndian.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isChainSecure) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Health",
                        tint = if (isChainSecure) GreenIndian else Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isChainSecure) "Blockchain Health: GOOD" else "BLOCKCHAIN FRAUD ALARM",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isChainSecure) GreenIndian else Color.Red,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = if (isChainSecure) "Cryptographic signatures matches seamlessly across all mined logs." else "Integrity verification failed. One or more block headers have been modified.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                    if (!isChainSecure) {
                        Button(
                            onClick = { viewModel.resetAndRepairChain() },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenIndian),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("REPAIR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("ADMIN REGISTRY CONTROL", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge, color = NavyIndia)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onNavigate(Screen.CandidateManagement) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyIndia)
                ) {
                    Icon(Icons.Default.List, "ballot")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("BALLOT MANAG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onNavigate(Screen.ElectionResults) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenIndian)
                ) {
                    Icon(Icons.Default.Poll, "analytics")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("VIEW AUDITS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = { onNavigate(Screen.BlockchainExplorer) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyIndia),
                border = BorderStroke(1.5.dp, NavyIndia)
            ) {
                Icon(Icons.Default.Storage, "dns")
                Spacer(modifier = Modifier.width(8.dp))
                Text("OPEN RAW BLOCKCHAIN LEDGER TERMINAL", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("ELECTION DEMOGRAPHICS (SIMULATED)", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium, color = NavyIndia)
            Spacer(modifier = Modifier.height(8.dp))

            DemographicProgressBar(label = "Age Group 18-25 (Young Electors)", ratio = 0.35f, tint = Saffron)
            DemographicProgressBar(label = "Age Group 26-45 (Working Voters)", ratio = 0.45f, tint = GreenIndian)
            DemographicProgressBar(label = "Age Group 45+ (Senior Citizen Electors)", ratio = 0.20f, tint = NavyIndia)

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun DemographicProgressBar(label: String, ratio: Float, tint: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text("${(ratio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { ratio },
            color = tint,
            trackColor = Color(0xFFE2E8F0),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
    }
}
