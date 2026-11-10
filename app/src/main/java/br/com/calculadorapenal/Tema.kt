package br.com.calculadorapenal

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AzulProfundo = Color(0xFF172638)
val FundoClaro = Color(0xFFF4F6F8)
val TextoSecundario = Color(0xFF5D6D7D)

private val cores = lightColorScheme(
    primary = AzulProfundo,
    onPrimary = Color.White,
    background = FundoClaro,
    onBackground = AzulProfundo,
    surface = Color.White,
    onSurface = AzulProfundo,
    surfaceVariant = Color(0xFFEAF0F5),
    outline = Color(0xFFC9D3DE),
    error = Color(0xFFAF3030)
)

@Composable
fun TemaCalculadora(conteudo: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = cores,
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(10.dp),
            large = RoundedCornerShape(12.dp)
        ),
        content = conteudo
    )
}
