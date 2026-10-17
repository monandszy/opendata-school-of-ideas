package dev.monand.opendata.feature.heatmap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.monand.opendata.data.model.LocationInspection
import dev.monand.opendata.data.model.PogodaStation

@Composable
fun ApiErrorDialog(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Błąd połączenia z API Warszawy")
        },
        text = {
            Text(
                text = "Nie udało się pobrać danych pogodowych z portalu dane.um.warszawa.pl.\n\nSzczegóły: $errorMessage",
            )
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text("Spróbuj ponownie")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        },
    )
}

@Composable
fun MapZoomControls(
    zoom: Double,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetView: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedZoom = kotlin.math.round(zoom * 10.0) / 10.0
    Column(
        modifier = modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.End,
    ) {
        val btnColors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        Button(
            onClick = onZoomIn,
            colors = btnColors,
        ) {
            Text("+ Zoom")
        }
        Button(
            onClick = onZoomOut,
            colors = btnColors,
        ) {
            Text("- Zoom")
        }
        OutlinedButton(
            onClick = onResetView,
            colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text("Warszawa (z=$formattedZoom)")
        }
    }
}

@Composable
fun StationDetailCard(
    station: PogodaStation,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(12.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 6.dp,
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = station.lokalizacja,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Temperatura powietrza: ${station.tempPow}°C",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (station.tempNaw != null) {
                Text(
                    text = "Temperatura nawierzchni: ${station.tempNaw}°C",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (station.wilgPow != null) {
                Text(
                    text = "Wilgotność: ${station.wilgPow}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (station.cisnienie != null) {
                Text(
                    text = "Ciśnienie: ${station.cisnienie} hPa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (station.predWiatru != null) {
                Text(
                    text = "Wiatr: ${station.predWiatru} m/s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (station.ijp != null) {
                Text(
                    text = "Jakość powietrza (IJP): ${station.ijp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (station.czas != null) {
                Text(
                    text = "Pomiar: ${station.czas}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "Współrzędne: ${station.lat}, ${station.lon}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun MapAttributionBadge(
    attribution: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(6.dp),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = attribution,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun LocationInspectionCard(
    inspection: LocationInspection,
    minTemp: Double,
    maxTemp: Double,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedTemp = kotlin.math.round(inspection.estimatedTemp * 10.0) / 10.0
    val formattedDist = kotlin.math.round(inspection.minDistanceKm * 10.0) / 10.0
    val formattedLat = kotlin.math.round(inspection.lat * 10000.0) / 10000.0
    val formattedLon = kotlin.math.round(inspection.lon * 10000.0) / 10000.0

    val badgeColor = heatColorForTemperatureCelsius(inspection.estimatedTemp, alpha = 1.0f)

    Surface(
        modifier = modifier.padding(12.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 6.dp,
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Sprawdzenie temperatury",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TextButton(onClick = onClose) {
                    Text("Zamknij")
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor,
            ) {
                Text(
                    text = "$formattedTemp °C (Szacunek IDW)",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }

            Text(
                text = "Najbliższa stacja: ${inspection.nearestStationName} ($formattedDist km)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "Współrzędne: $formattedLat, $formattedLon",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
