package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DefaultWorldCities
import com.example.data.WorldCity
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun WorldClockScreen(
    favoriteCityIds: Set<String>,
    is24Hour: Boolean,
    onToggleFavoriteCity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddCityDialog by remember { mutableStateOf(false) }
    var currentTick by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Live update every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTick = System.currentTimeMillis()
        }
    }

    val allCities = remember { DefaultWorldCities.allCities }
    val displayCities = remember(favoriteCityIds) {
        allCities.filter { favoriteCityIds.contains(it.id) }
    }

    val localZoneId = remember { ZoneId.systemDefault() }
    val localZdt = remember(currentTick) { ZonedDateTime.now(localZoneId) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("world_clock_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header & Add button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الساعة العالمية",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "متابعة فروق التوقيت حول العالم",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }

                    Button(
                        onClick = { showAddCityDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("add_world_city_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة مدينة", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }

            // Local Time Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "توقيتك المحلي",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = localZoneId.id,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            val pattern = if (is24Hour) "HH:mm" else "hh:mm"
                            val timeStr = localZdt.format(java.time.format.DateTimeFormatter.ofPattern(pattern, java.util.Locale.US))
                            val amPm = if (localZdt.hour < 12) "ص" else "م"

                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = Color.White
                            )
                            if (!is24Hour) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = amPm,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AccentCyan,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Cities List
            items(displayCities, key = { it.id }) { city ->
                val isDay = city.isDaytime()
                val diffText = city.getTimeDifferenceArabic(localZoneId)
                val timeFormatted = city.getTimeFormatted(is24Hour)
                val amPm = city.getAmPmArabic()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("world_city_${city.id}"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = city.nameAr,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                    contentDescription = null,
                                    tint = if (isDay) AccentOrange else PrimaryPurpleLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${city.countryAr} • $diffText",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = timeFormatted,
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp
                                    ),
                                    color = Color.White
                                )
                                if (!is24Hour) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = amPm,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AccentOrange,
                                        modifier = Modifier.padding(bottom = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = { onToggleFavoriteCity(city.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إزالة المدينة",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add City Modal Dialog
        if (showAddCityDialog) {
            AddCityDialog(
                allCities = allCities,
                favoriteCityIds = favoriteCityIds,
                onToggleCity = onToggleFavoriteCity,
                onDismiss = { showAddCityDialog = false }
            )
        }
    }
}

@Composable
private fun AddCityDialog(
    allCities: List<WorldCity>,
    favoriteCityIds: Set<String>,
    onToggleCity: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) allCities
        else allCities.filter {
            it.nameAr.contains(searchQuery, ignoreCase = true) ||
            it.nameEn.contains(searchQuery, ignoreCase = true) ||
            it.countryAr.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("add_city_dialog"),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إضافة مدن للساعة العالمية",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن مدينة أو دولة...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryPurple)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_city_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCities, key = { it.id }) { city ->
                        val isAdded = favoriteCityIds.contains(city.id)
                        Surface(
                            onClick = { onToggleCity(city.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isAdded) PrimaryPurple.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = city.nameAr,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${city.countryAr} (${city.nameEn})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryDark
                                    )
                                }

                                Checkbox(
                                    checked = isAdded,
                                    onCheckedChange = { onToggleCity(city.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryPurple,
                                        uncheckedColor = TextMutedDark
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
