package dev.monand.opendata

import dev.monand.opendata.api.model.MonitoringJakosciPowietrzaItem
import dev.monand.opendata.api.model.MonitoringJakosciPowietrzaItemDataInner
import dev.monand.opendata.api.model.MonitoringJakosciPowietrzaItemIjp
import dev.monand.opendata.api.model.PogodaItem
import dev.monand.opendata.api.model.SensorCommunityItem
import dev.monand.opendata.api.model.SensorDataValue
import dev.monand.opendata.api.model.SensorInfo
import dev.monand.opendata.api.model.SensorLocation
import dev.monand.opendata.api.model.SensorType
import dev.monand.opendata.core.geo.IdwInterpolator
import dev.monand.opendata.core.geo.WebMercator
import dev.monand.opendata.core.map.MapOverlayGeoJson
import dev.monand.opendata.core.map.MapTileSource
import dev.monand.opendata.data.model.PogodaStation
import dev.monand.opendata.data.model.toDomainOrNull
import dev.monand.opendata.data.repository.DefaultPogodaRepository
import dev.monand.opendata.feature.heatmap.components.heatColorForTemperatureCelsius
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SharedCommonTest {
    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun idwInterpolationMatchesStationValue() {
        val stations =
            listOf(
                PogodaStation("Stacja A", 52.29241, 20.97288, tempPow = 14.1),
                PogodaStation("Stacja B", 52.22430, 21.20674, tempPow = 11.7),
            )
        val first = stations.first()
        val interpolated = IdwInterpolator.interpolate(first.lat, first.lon, stations).temp
        assertTrue(abs(first.tempPow - interpolated) < 1e-4)
    }

    @Test
    fun webMercatorLatLonRoundTripMatchesOriginal() {
        val lat = 52.2297
        val lon = 21.0122
        val zoom = 11
        val (recoveredLat, recoveredLon) =
            WebMercator.screenOffsetToLatLon(
                screenX = 500f,
                screenY = 400f,
                centerLat = lat,
                centerLon = lon,
                zoom = zoom,
                widthPx = 1000f,
                heightPx = 800f,
            )
        assertTrue(abs(lat - recoveredLat) < 1e-6)
        assertTrue(abs(lon - recoveredLon) < 1e-6)
    }

    @Test
    fun openFreeMap3dStyleAndFallbackTileUrlAreCorrect() {
        assertEquals(
            "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json",
            MapTileSource.OpenFreeMap3D.styleUrl,
        )
        val url = MapTileSource.OpenFreeMap3D.urlTemplate(11, 1143, 674)
        assertEquals("https://a.basemaps.cartocdn.com/rastertiles/dark_all/11/1143/674.png", url)
    }

    @Test
    fun mapOverlayGeoJsonGeneratesTextureAndStationCollection() {
        val stations =
            listOf(
                PogodaStation("Stacja A", 52.29241, 20.97288, tempPow = 14.1),
                PogodaStation("Stacja B", 52.22430, 21.20674, tempPow = 11.7),
            )
        val texture =
            MapOverlayGeoJson.generateIdwTexture(
                stations = stations,
                minTemp = 11.7,
                maxTemp = 14.1,
                width = 16,
                height = 16,
            )
        assertNotNull(texture)
        assertEquals(16, texture.width)
        assertEquals(16, texture.height)
        assertEquals(16 * 16 * 4, texture.rgbaBytes.size)

        val geoJson =
            MapOverlayGeoJson.buildStationsGeoJson(
                stations = stations,
                selectedStation = stations.first(),
                minTemp = 11.7,
                maxTemp = 14.1,
            )
        assertTrue(geoJson.contains("\"type\":\"FeatureCollection\""))
        assertTrue(geoJson.contains("Stacja A"))
    }

    @Test
    fun heatmapUiStateHasCorrectPanLimitsAndShowStationsToggle() {
        val state = dev.monand.opendata.feature.heatmap.HeatmapUiState()
        assertTrue(state.showStations)
        assertEquals(51.90, dev.monand.opendata.feature.heatmap.HeatmapUiState.MIN_PAN_LAT)
        assertEquals(52.55, dev.monand.opendata.feature.heatmap.HeatmapUiState.MAX_PAN_LAT)
        assertEquals(20.50, dev.monand.opendata.feature.heatmap.HeatmapUiState.MIN_PAN_LON)
        assertEquals(21.50, dev.monand.opendata.feature.heatmap.HeatmapUiState.MAX_PAN_LON)
        assertEquals(9.0, dev.monand.opendata.feature.heatmap.HeatmapUiState.MIN_ZOOM)
    }

    @Test
    fun pogodaItemMapsToDomainStation() {
        val dto =
            PogodaItem(
                lokalizacja = "Stacja Bielany",
                lat = 52.29241,
                lon = 20.97288,
                czas = "2026-10-17 13:20:00",
                tempPow = 13.4,
                tempNaw = 24.8,
                wilgPow = 51.7,
                predWiatru = 1.6,
                kierWiatru = 193.0,
                intOpadu = 0.0,
            )
        val station = dto.toDomainOrNull()
        assertNotNull(station)
        assertEquals("Stacja Bielany", station.lokalizacja)
        assertEquals(13.4, station.tempPow)
        assertEquals(24.8, station.tempNaw)
        assertEquals("2026-10-17 13:20:00", station.czas)
    }

    @Test
    fun pogodaItemWithMissingCoordinatesReturnsNull() {
        val invalid = PogodaItem(lokalizacja = "Brak", lat = null, lon = 21.0, tempPow = 14.0)
        assertNull(invalid.toDomainOrNull())
    }

    @Test
    fun airQualityItemWithTemperatureMapsToDomainStation() {
        val dto =
            MonitoringJakosciPowietrzaItem(
                name = "Warszawa-263946",
                station = "Latarnia Krucza-Hoża",
                lat = 52.2279,
                lon = 21.0177,
                ijp = MonitoringJakosciPowietrzaItemIjp(name = "Bardzo dobry"),
                data =
                listOf(
                    MonitoringJakosciPowietrzaItemDataInner(
                        paramCode = "PM10",
                        value = 11.2,
                        time = "2026-10-17 14:20:00",
                        unit = "µg/m3",
                    ),
                    MonitoringJakosciPowietrzaItemDataInner(
                        paramCode = "T",
                        value = 12.03,
                        time = "2026-10-17 14:20:00",
                        unit = "C",
                    ),
                    MonitoringJakosciPowietrzaItemDataInner(
                        paramCode = "H",
                        value = 63.71,
                        time = "2026-10-17 14:20:00",
                        unit = "%",
                    ),
                    MonitoringJakosciPowietrzaItemDataInner(
                        paramCode = "P",
                        value = 1006.19,
                        time = "2026-10-17 14:20:00",
                        unit = "hPa",
                    ),
                ),
            )
        val station = dto.toDomainOrNull()
        assertNotNull(station)
        assertEquals("Latarnia Krucza-Hoża (Warszawa-263946)", station.lokalizacja)
        assertEquals(12.03, station.tempPow)
        assertEquals(63.71, station.wilgPow)
        assertEquals(1006.19, station.cisnienie)
        assertEquals("Bardzo dobry", station.ijp)
        assertEquals("2026-10-17 14:20:00", station.czas)
    }

    @Test
    fun airQualityItemWithoutTemperatureReturnsNull() {
        val dto =
            MonitoringJakosciPowietrzaItem(
                name = "Warszawa-Grochowska",
                station = "ul. Grochowska",
                lat = 52.245703,
                lon = 21.080555,
                data =
                listOf(
                    MonitoringJakosciPowietrzaItemDataInner(
                        paramCode = "PM10",
                        value = 9.67,
                        time = "2026-10-17 14:00:00",
                    ),
                ),
            )
        assertNull(dto.toDomainOrNull())
    }

    @Test
    fun sensorCommunityOutdoorItemMapsToDomainStation() {
        val item =
            SensorCommunityItem(
                id = 88123L,
                timestamp = "2026-10-17 17:00:00",
                location =
                SensorLocation(
                    id = 1234L,
                    latitude = "52.2297",
                    longitude = "21.0122",
                    indoor = 0,
                ),
                sensor = SensorInfo(sensorType = SensorType(name = "BME280")),
                sensordatavalues =
                listOf(
                    SensorDataValue(value = "11.40", valueType = "temperature"),
                    SensorDataValue(value = "72.10", valueType = "humidity"),
                    SensorDataValue(value = "101325.00", valueType = "pressure"),
                ),
            )
        val station = item.toDomainOrNull()
        assertNotNull(station)
        assertEquals("Sensor.Community #88123 (BME280)", station.lokalizacja)
        assertEquals(52.2297, station.lat)
        assertEquals(21.0122, station.lon)
        assertEquals(11.40, station.tempPow)
        assertEquals(72.10, station.wilgPow)
        assertEquals(1013.25, station.cisnienie)
    }

    @Test
    fun sensorCommunityIndoorItemReturnsNull() {
        val item =
            SensorCommunityItem(
                id = 99999L,
                location = SensorLocation(latitude = "52.22", longitude = "21.01", indoor = 1),
                sensordatavalues = listOf(SensorDataValue(value = "22.50", valueType = "temperature")),
            )
        assertNull(item.toDomainOrNull())
    }

    @Test
    fun heatColorForTemperatureCelsiusMaps20DegreesToPureYellow() {
        val yellowColor = heatColorForTemperatureCelsius(20.0, alpha = 1.0f)
        assertEquals(1.0f, yellowColor.red, 1e-3f)
        assertEquals(1.0f, yellowColor.green, 1e-3f)
        assertEquals(0.0f, yellowColor.blue, 1e-3f)

        val coldColor = heatColorForTemperatureCelsius(0.0, alpha = 1.0f)
        assertEquals(0.0f, coldColor.red, 1e-3f)
        assertTrue(coldColor.blue > coldColor.green)

        val hotColor = heatColorForTemperatureCelsius(30.0, alpha = 1.0f)
        assertEquals(1.0f, hotColor.red, 1e-3f)
        assertTrue(hotColor.green < 0.2f)
        assertEquals(0.0f, hotColor.blue, 1e-3f)
    }
}
