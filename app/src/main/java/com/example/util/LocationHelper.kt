package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import java.util.Locale

object LocationHelper {

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onSuccess: (lat: Double, lng: Double, cityName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onError("خدمة تحديد الموقع غير متوفرة على هذا الجهاز")
            return
        }

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            onError("يرجى تفعيل خدمة الموقع (GPS) من إعدادات الجهاز")
            return
        }

        var bestLocation: Location? = null
        try {
            val gpsLocation = if (isGpsEnabled) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
            val netLocation = if (isNetworkEnabled) locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) else null

            bestLocation = when {
                gpsLocation != null && netLocation != null -> {
                    if (gpsLocation.time > netLocation.time) gpsLocation else netLocation
                }
                gpsLocation != null -> gpsLocation
                netLocation != null -> netLocation
                else -> null
            }
        } catch (e: SecurityException) {
            onError("لم يتم منح إذن الوصول إلى الموقع")
            return
        } catch (e: Exception) {
            onError("تعذر جلب الموقع: ${e.message}")
            return
        }

        if (bestLocation != null) {
            val lat = bestLocation.latitude
            val lng = bestLocation.longitude
            val cityName = resolveCityName(context, lat, lng)
            onSuccess(lat, lng, cityName)
        } else {
            // Default to Makkah if no last known location is yet cached
            onSuccess(21.4225, 39.8262, "مكة المكرمة (تقديري)")
        }
    }

    private fun resolveCityName(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale("ar"))
            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                val country = addr.countryName
                when {
                    locality != null && country != null -> "$locality، $country"
                    locality != null -> locality
                    country != null -> country
                    else -> String.format(Locale.US, "خط عرض %.2f، خط طول %.2f", lat, lng)
                }
            } else {
                String.format(Locale.US, "الموقع الحالي (%.2f, %.2f)", lat, lng)
            }
        } catch (e: Exception) {
            String.format(Locale.US, "الموقع الحالي (%.2f, %.2f)", lat, lng)
        }
    }
}
