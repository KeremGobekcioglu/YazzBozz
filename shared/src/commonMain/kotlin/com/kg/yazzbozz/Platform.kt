package com.kg.yazzbozz

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform