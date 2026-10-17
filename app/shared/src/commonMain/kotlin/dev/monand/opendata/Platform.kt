package dev.monand.opendata

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
