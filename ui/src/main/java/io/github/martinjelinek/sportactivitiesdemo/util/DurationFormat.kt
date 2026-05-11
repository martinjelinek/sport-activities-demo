package io.github.martinjelinek.sportactivitiesdemo.util

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
