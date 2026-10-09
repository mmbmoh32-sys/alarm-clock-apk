package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.ShakeDetector
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun DismissMissionDialog(
    missionType: String, // "MATH", "SHAKE", "TYPING"
    onMissionSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Prevent dismissing by tapping outside during alarm */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(28.dp))
                .testTag("dismiss_mission_dialog"),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (missionType) {
                    "MATH" -> MathChallenge(onSuccess = onMissionSuccess)
                    "SHAKE" -> ShakeChallenge(onSuccess = onMissionSuccess)
                    "TYPING" -> TypingChallenge(onSuccess = onMissionSuccess)
                    else -> {
                        // Fallback
                        LaunchedEffect(Unit) { onMissionSuccess() }
                    }
                }
            }
        }
    }
}

@Composable
private fun MathChallenge(onSuccess: () -> Unit) {
    var num1 by remember { mutableIntStateOf(Random.nextInt(15, 45)) }
    var num2 by remember { mutableIntStateOf(Random.nextInt(12, 35)) }
    var userAnswer by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val correctAnswer = num1 + num2

    Icon(
        imageVector = Icons.Default.Calculate,
        contentDescription = null,
        tint = PrimaryPurple,
        modifier = Modifier.size(48.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "مهمة الحساب الذهني",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = Color.White
    )

    Text(
        text = "قم بحل المعادلة التالية لإيقاف المنبه",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondaryDark
    )

    Spacer(modifier = Modifier.height(20.dp))

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$num1 + $num2 = ؟",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = AccentOrangeLight,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 18.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = userAnswer,
        onValueChange = {
            userAnswer = it.filter { char -> char.isDigit() }
            isError = false
        },
        placeholder = { Text("أدخل الناتج هنا", textAlign = TextAlign.Center) },
        singleLine = true,
        isError = isError,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("math_answer_input"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryPurple,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )

    if (isError) {
        Text(
            text = "إجابة غير صحيحة، حاول مجدداً!",
            color = AccentRed,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
        onClick = {
            if (userAnswer.toIntOrNull() == correctAnswer) {
                onSuccess()
            } else {
                isError = true
                userAnswer = ""
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("verify_math_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
    ) {
        Text("تأكيد الإجابة وإيقاف المنبه", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun ShakeChallenge(onSuccess: () -> Unit) {
    val totalRequiredShakes = 15
    var shakesCount by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val detector = ShakeDetector(context) {
            shakesCount++
            if (shakesCount >= totalRequiredShakes) {
                onSuccess()
            }
        }
        detector.start()
        onDispose {
            detector.stop()
        }
    }

    val progress = (shakesCount.toFloat() / totalRequiredShakes).coerceIn(0f, 1f)

    Icon(
        imageVector = Icons.Default.Vibration,
        contentDescription = null,
        tint = AccentOrange,
        modifier = Modifier.size(48.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "مهمة هز الجهاز",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = Color.White
    )

    Text(
        text = "قم بهز هاتفك بقوة للاستيقاظ وإيقاف المنبه",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondaryDark
    )

    Spacer(modifier = Modifier.height(24.dp))

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(140.dp)
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = AccentOrange,
            trackColor = DarkSurfaceVariant,
            strokeWidth = 10.dp
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${totalRequiredShakes - shakesCount}",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "هزة متبقية",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Interactive button for users who test on emulator or desk
    OutlinedButton(
        onClick = {
            shakesCount++
            if (shakesCount >= totalRequiredShakes) {
                onSuccess()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("shake_button_manual"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentOrange)
    ) {
        Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("هز تجريبي باللمس", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun TypingChallenge(onSuccess: () -> Unit) {
    val sentences = listOf(
        "صباح النشاط والبدايات الجميلة",
        "اليوم يوم جديد لتحقيق الأهداف",
        "العزيمة تصنع المستحيل كل صباح",
        "أنا مستعد ليوم رائع ومليء بالإنجاز"
    )
    val targetSentence = remember { sentences.random() }
    var userText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Icon(
        imageVector = Icons.Default.Keyboard,
        contentDescription = null,
        tint = PrimaryPurple,
        modifier = Modifier.size(48.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "مهمة كتابة الجملة",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = Color.White
    )

    Text(
        text = "اكتب العبارة التالية بدقة لإيقاف المنبه",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondaryDark
    )

    Spacer(modifier = Modifier.height(18.dp))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "« $targetSentence »",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryPurpleLight,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = userText,
        onValueChange = {
            userText = it
            isError = false
        },
        placeholder = { Text("اكتب العبارة هنا باللغة العربية") },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("typing_mission_input"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryPurple,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )

    if (isError) {
        Text(
            text = "العبارة غير مطابقة، يرجى كتابتها تماماً",
            color = AccentRed,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
        onClick = {
            if (userText.trim() == targetSentence.trim()) {
                onSuccess()
            } else {
                isError = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("verify_typing_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
    ) {
        Text("تحقق وإيقاف المنبه", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}
