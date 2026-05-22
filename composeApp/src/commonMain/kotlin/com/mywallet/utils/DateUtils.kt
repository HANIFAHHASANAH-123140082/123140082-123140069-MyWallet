package com.mywallet.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatMillisToDate(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    
    // Using dayOfMonth and monthNumber as they are common, 
    // but adapting to what the analyzer suggests if needed.
    // Actually dayOfMonth is usually fine in stable versions.
    return "${dateTime.dayOfMonth} ${monthNames[dateTime.monthNumber - 1]} ${dateTime.year}"
}
