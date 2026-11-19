package com.kg.yazzbozz.platform

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatGameDate(epochMillis: Long): String {
    val formatter = NSDateFormatter().apply {
        locale = NSLocale(localeIdentifier = "tr_TR")
        dateFormat = "d MMMM yyyy, HH:mm"
    }
    return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0))
}
