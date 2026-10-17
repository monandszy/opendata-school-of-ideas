package dev.monand.opendata

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = sayHello(platform.name)
}
