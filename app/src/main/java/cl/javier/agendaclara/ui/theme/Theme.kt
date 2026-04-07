package cl.javier.agendaclara.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import cl.javier.agendaclara.data.model.AppSettings

@Composable
fun AgendaTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val colors = if (settings.highContrast) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colors, typography = MaterialTheme.typography.copy(bodyLarge = TextStyle(fontSize = if (settings.largeText) 20.sp else 16.sp)), content = content)
}
