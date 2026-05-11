package io.github.martinjelinek.sportactivitiesdemo.domain.location

import io.github.martinjelinek.sportactivitiesdemo.domain.model.Coordinates

/**
 * Reads the device's current geographic position and resolves it to a
 * human-readable place name. Pure-Kotlin contract; the Android-backed
 * implementation lives in :data.
 */
interface LocationProvider {

    /** @return the last known device coordinates, or null if unavailable. */
    suspend fun currentCoordinates(): Coordinates?

    /** @return a short place description (e.g. "Stromovka, Prague"), or null if reverse geocoding fails. */
    suspend fun getLocationDescription(coordinates: Coordinates): String?
}
