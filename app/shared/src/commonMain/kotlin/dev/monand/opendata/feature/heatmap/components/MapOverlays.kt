package dev.monand.opendata.feature.heatmap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
        Button(onClick = onZoomIn) {
            Text("+ Zoom")
        }
        Button(onClick = onZoomOut) {
            Text("- Zoom")
        }
        OutlinedButton(onClick = onResetView) {
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
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = station.lokalizacja,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "Temperatura powietrza: ${station.tempPow}°C",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (station.tempNaw != null) {
                Text(
                    text = "Temperatura nawierzchni: ${station.tempNaw}°C",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (station.wilgPow != null) {
                Text(
                    text = "Wilgotność: ${station.wilgPow}%",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (station.cisnienie != null) {
                Text(
                    text = "Ciśnienie: ${station.cisnienie} hPa",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (station.predWiatru != null) {
                Text(
                    text = "Wiatr: ${station.predWiatru} m/s",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (station.ijp != null) {
                Text(
                    text = "Jakość powietrza (IJP): ${station.ijp}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (station.czas != null) {
                Text(
                    text = "Pomiar: ${station.czas}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Text(
                text = "Współrzędne: ${station.lat}, ${station.lon}",
                style = MaterialTheme.typography.bodySmall,
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
        color = Color.White.copy(alpha = 0.85f),
    ) {
        Text(
            text = attribution,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
