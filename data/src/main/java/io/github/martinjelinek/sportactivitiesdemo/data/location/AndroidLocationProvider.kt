package io.github.martinjelinek.sportactivitiesdemo.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.martinjelinek.sportactivitiesdemo.domain.location.LocationProvider
import io.github.martinjelinek.sportactivitiesdemo.domain.model.Coordinates
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Singleton
class AndroidLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationProvider {

    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Permission is checked by the UI layer before this is invoked; we still
    // suppress lint here because the static analyser can't see across that boundary.
    @SuppressLint("MissingPermission")
    override suspend fun currentCoordinates(): Coordinates? = try {
        val loc = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
            ?: client.lastLocation.await()
        loc?.let { Coordinates(latitude = it.latitude, longitude = it.longitude) }
    } catch (e: SecurityException) {
        // No permission at runtime — should not happen given the UI gate,
        // but treat as "unavailable" rather than crashing the VM.
        null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    override suspend fun getLocationDescription(coordinates: Coordinates): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                Geocoder(context, Locale.getDefault())
                    .getFromLocation(coordinates.latitude, coordinates.longitude, 1)
                    ?.firstOrNull()
                    ?.let { addr ->
                        // Prefer "Locality, Country" — fall back to whatever the geocoder gave us.
                        listOfNotNull(addr.locality ?: addr.subAdminArea, addr.countryName)
                            .joinToString(", ")
                            .ifBlank { addr.getAddressLine(0) }
                    }
            }.onFailure { if (it is CancellationException) throw it }.getOrNull()
        }
}
