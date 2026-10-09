package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.AlarmSoundManager
import com.example.ui.theme.*
import com.example.util.HijriDateUtil
import com.example.util.PrayerTime
import com.example.util.PrayerTimesCalculator
import com.example.viewmodel.AlarmViewModel

@Composable
fun PrayerTimesDialog(
    viewModel: AlarmViewModel,
    is24Hour: Boolean,
    soundManager: AlarmSoundManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val locationName by viewModel.locationDisplayName.collectAsStateWithLifecycle()
    val prayers by viewModel.prayerTimesState.collectAsStateWithLifecycle()

    var isLocating by remember { mutableStateOf(false) }
    var locationStatusMessage by remember { mutableStateOf<String?>(null) }
    var prayerSelectingMuadhin by remember { mutableStateOf<PrayerTime?>(null) }

    val hijriDate = remember { HijriDateUtil.getTodayHijriArabic() }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isLocating = true
            viewModel.requestGpsLocation(context) { success, msg ->
                isLocating = false
                locationStatusMessage = if (success) "تم تحديد موقعك بدقة: $msg" else msg
            }
        } else {
            locationStatusMessage = "تم رفض إذن الموقع، يمكنك اختيار المدينة يدوياً"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.stop()
        }
    }

    val quickCities = listOf(
        "makkah" to "مكة المكرمة",
        "madinah" to "المدينة المنورة",
        "jerusalem" to "القدس الشريف",
        "riyadh" to "الرياض",
        "cairo" to "القاهرة",
        "dubai" to "دبي",
        "amman" to "عمّان",
        "kuwait" to "الكويت",
        "doha" to "الدوحة"
    )

    Dialog(
        onDismissRequest = {
            soundManager.stop()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("prayer_times_dialog"),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AccentCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Mosque,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "مواقيت الصلاة والأذان",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = hijriDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryPurpleLight
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            soundManager.stop()
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GPS Location Detection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "الموقع الحالي المعتمد:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMutedDark
                                    )
                                    Text(
                                        text = locationName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("detect_gps_button")
                            ) {
                                if (isLocating) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تحديد GPS", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        if (locationStatusMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = locationStatusMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick City Override Chips
                Text("أو اختر مدينة رئيسية:", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickCities) { (cityId, cityName) ->
                        Surface(
                            onClick = { viewModel.setPrayerCityPreset(cityId) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (locationName.contains(cityName)) AccentOrange.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            border = if (locationName.contains(cityName)) BorderStroke(1.dp, AccentOrange) else null
                        ) {
                            Text(
                                text = cityName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (locationName.contains(cityName)) AccentOrangeLight else TextSecondaryDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prayers List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(prayers, key = { it.id }) { prayer ->
                        val isSunrise = prayer.id == "sunrise"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (prayer.isNext) DarkSurfaceVariant else DarkSurfaceCard
                            ),
                            border = if (prayer.isNext) BorderStroke(1.5.dp, AccentOrange) else BorderStroke(0.8.dp, DarkSurfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                // Top Row: Prayer name, Next badge, Time, and Athan Switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = prayer.nameAr,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (prayer.isNext) Color.White else TextPrimaryDark
                                        )

                                        if (prayer.isNext) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = AccentOrange
                                            ) {
                                                Text(
                                                    text = "الصلاة القادمة",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = prayer.getFormattedTime(is24Hour),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            ),
                                            color = if (prayer.isNext) AccentOrangeLight else Color.White
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Athan Toggle
                                        Switch(
                                            checked = prayer.isAthanEnabled,
                                            onCheckedChange = { viewModel.togglePrayerAthan(prayer.id, it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = if (prayer.isNext) AccentOrange else PrimaryPurple,
                                                uncheckedThumbColor = TextMutedDark,
                                                uncheckedTrackColor = DarkSurfaceVariant
                                            ),
                                            modifier = Modifier.testTag("athan_switch_${prayer.id}")
                                        )
                                    }
                                }

                                // Bottom Row: Muadhin Voice Selector (for prayers with Athan)
                                if (!isSunrise) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = DarkSurface.copy(alpha = 0.5f), thickness = 0.6.dp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            onClick = { prayerSelectingMuadhin = prayer },
                                            shape = RoundedCornerShape(12.dp),
                                            color = DarkSurface,
                                            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.35f)),
                                            modifier = Modifier.testTag("muadhin_selector_${prayer.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.RecordVoiceOver,
                                                    contentDescription = null,
                                                    tint = PrimaryPurpleLight,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = prayer.getMuadhinDisplayName(),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                    color = TextPrimaryDark
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = TextSecondaryDark,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        // Quick Preview Muadhin Audio Button
                                        IconButton(
                                            onClick = {
                                                soundManager.playAlarm(
                                                    soundId = prayer.muadhinId,
                                                    isVibrate = false,
                                                    isGradualVolume = false,
                                                    isPreview = true
                                                )
                                            },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(DarkSurface)
                                                .testTag("preview_muadhin_${prayer.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.VolumeUp,
                                                contentDescription = "استماع للأذان",
                                                tint = AccentCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet / Dialog to pick Muadhin for a prayer
    prayerSelectingMuadhin?.let { targetPrayer ->
        MuadhinSelectionDialog(
            prayerName = targetPrayer.nameAr,
            currentMuadhinId = targetPrayer.muadhinId,
            soundManager = soundManager,
            onSelect = { selectedMuadhinId ->
                viewModel.setPrayerMuadhin(targetPrayer.id, selectedMuadhinId)
                prayerSelectingMuadhin = null
            },
            onDismiss = {
                soundManager.stop()
                prayerSelectingMuadhin = null
            }
        )
    }
}

@Composable
private fun MuadhinSelectionDialog(
    prayerName: String,
    currentMuadhinId: String,
    soundManager: AlarmSoundManager,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var previewingId by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {
            soundManager.stop()
            onDismiss()
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp))
                .testTag("muadhin_dialog"),
            color = DarkSurface,
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تخصيص صوت الأذان",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "أذان صلاة $prayerName",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryPurpleLight
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrayerTimesCalculator.availableMuadhins.forEach { (id, name) ->
                        val isSelected = currentMuadhinId == id
                        val isPlayingThis = previewingId == id

                        Surface(
                            onClick = { onSelect(id) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) PrimaryPurple.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            border = if (isSelected) BorderStroke(1.5.dp, PrimaryPurple) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("muadhin_option_$id")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSelect(id) },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else TextPrimaryDark
                                    )
                                }

                                // Preview button for this muadhin
                                IconButton(
                                    onClick = {
                                        if (isPlayingThis) {
                                            soundManager.stop()
                                            previewingId = null
                                        } else {
                                            previewingId = id
                                            soundManager.playAlarm(
                                                soundId = id,
                                                isVibrate = false,
                                                isGradualVolume = false,
                                                isPreview = true,
                                                onStop = { previewingId = null }
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlayingThis) AccentOrange else DarkSurface)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "معاينة الصوت",
                                        tint = if (isPlayingThis) Color.White else AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        soundManager.stop()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("تم الاختيار", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
