package com.kg.yazzbozz.platform

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun formatGameDate(epochMillis: Long): String =
    SimpleDateFormat(GameDatePattern, Locale.forLanguageTag("tr-TR")).format(Date(epochMillis))

private const val GameDatePattern = "d MMMM yyyy, HH:mm"
