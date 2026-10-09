package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.PreferencesManager
import com.example.ui.theme.*
import com.example.util.HijriDateUtil
import com.example.util.PrayerTimesCalculator

@Composable
fun PrayerTimesDialog(
    prefs: PreferencesManager,
    is24Hour: Boolean,
    onDismiss: () -> Unit
) {
    var selectedCityId by remember { mutableStateOf(prefs.selectedPrayerCityId) }
    val prayerTimes = remember(selectedCityId, is24Hour) {
        PrayerTimesCalculator.calculatePrayers(selectedCityId, is24Hour)
    }

    val hijriDate = remember { HijriDateUtil.getTodayHijriArabic() }

    val citiesList = listOf(
        "makkah" to "مكة المكرمة",
        "jerusalem" to "القدس الشريف",
        "cairo" to "القاهرة",
        "riyadh" to "الرياض",
        "dubai" to "دبي",
        "amman" to "عمّان",
        "doha" to "الدوحة",
        "kuwait" to "الكويت",
        "london" to "لندن"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(28.dp))
                .testTag("prayer_times_dialog"),
            color = DarkSurface
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mosque, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مواقيت الصلاة",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondaryDark)
                    }
                }

                Text(
                    text = "$hijriDate • مواقيت دقيقة حسب التقويم الفلكي",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryPurpleLight
                )

                Spacer(modifier = Modifier.height(16.dp))

                // City Selector Chips
                Text("اختر المدينة:", style = MaterialTheme.typography.labelMedium, color = TextSecondaryDark)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    citiesList.take(4).forEach { (id, name) ->
                        val isSelected = selectedCityId == id
                        Surface(
                            onClick = {
                                selectedCityId = id
                                prefs.selectedPrayerCityId = id
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryPurple else DarkSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = name.split(" ")[0],
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else TextSecondaryDark
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prayer Times Cards
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    prayerTimes.forEach { prayer ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (prayer.isNext) AccentOrange.copy(alpha = 0.18f) else DarkSurfaceVariant,
                            border = if (prayer.isNext) androidx.compose.foundation.BorderStroke(1.5.dp, AccentOrange) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = prayer.nameAr,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = prayer.getFormattedTime(is24Hour),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = if (prayer.isNext) AccentOrangeLight else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}
