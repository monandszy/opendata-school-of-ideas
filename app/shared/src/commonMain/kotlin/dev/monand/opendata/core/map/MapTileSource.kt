package dev.monand.opendata.core.map

data class TileCoord(
    val z: Int,
    val x: Int,
    val y: Int,
)

enum class MapTileSource(
    val label: String,
    val attribution: String,
    val styleUrl: String,
    val urlTemplate: (z: Int, x: Int, y: Int) -> String,
) {
    OpenFreeMap3D(
        label = "OpenFreeMap 3D (Dark)",
        attribution = "© OpenStreetMap contributors © CARTO © OpenMapTiles",
        styleUrl = "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json",
        urlTemplate = { z, x, y -> "https://a.basemaps.cartocdn.com/rastertiles/dark_all/$z/$x/$y.png" },
    ),
}
