package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Alarm
import com.example.util.PrayerTimesCalculator
import com.example.util.TimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("منبّه", appName)
    }

    @Test
    fun `test time formatting 12h and 24h`() {
        val (time12, amPm12) = TimeFormatter.formatTime(14, 30, false)
        assertEquals("02:30", time12)
        assertEquals("م", amPm12)

        val (time24, amPm24) = TimeFormatter.formatTime(14, 30, true)
        assertEquals("14:30", time24)
        assertEquals("", amPm24)
    }

    @Test
    fun `test prayer times calculation`() {
        val prayers = PrayerTimesCalculator.calculatePrayers("makkah", false)
        assertEquals(6, prayers.size)
        assertTrue(prayers.any { it.isNext })
    }

    @Test
    fun `test alarm days formatting`() {
        val dailyAlarm = Alarm(hour = 7, minute = 0, daysOfWeek = "1,2,3,4,5,6,7")
        assertEquals("يومياً", dailyAlarm.getFormattedDaysArabic())

        val onceAlarm = Alarm(hour = 7, minute = 0, daysOfWeek = "")
        assertEquals("مرة واحدة", onceAlarm.getFormattedDaysArabic())
    }

    @Test
    fun `test prayer times calculation with GPS coordinates and muadhins`() {
        val prayers = PrayerTimesCalculator.calculatePrayers(24.7136, 46.6753, false)
        assertEquals(6, prayers.size)
        val fajr = prayers.first { it.id == "fajr" }
        assertNotNull(fajr.getMuadhinDisplayName())
    }

    @Test
    fun `launch MainActivity successfully`() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        assertNotNull(controller.get())
    }
}
