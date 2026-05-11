package io.github.martinjelinek.sportactivitiesdemo.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm"

/**
 * @return duration in hh:mm:ss format.
 */
fun Long.formatDuration(): String {
    val totalSec = this / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

/**
 * @return timestamp in yyyy-MM-dd HH:mm format.
 */
fun formatTimestamp(epochMillis: Long): String =
    DateTimeFormatter
        .ofPattern(TIMESTAMP_PATTERN, Locale.getDefault())
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))