package br.com.calculadorapenal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TelaCalculadora()
            }
        }
    }
}

@Composable
private fun TelaCalculadora() {
    var anos by remember { mutableStateOf("") }
    var meses by remember { mutableStateOf("0") }
    var dias by remember { mutableStateOf("0") }
    var regime by remember { mutableStateOf(Regime.FECHADO) }
    var inicio by remember { mutableStateOf("") }
    var referencia by remember { mutableStateOf(LocalDate.now().toString()) }
    var resultado by remember { mutableStateOf<ResultadoPena?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }

    Scaffold { espacamento ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(espacamento)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Calculadora Penal", style = MaterialTheme.typography.headlineMedium)
            Text("Simulação cronológica acadêmica, sem validade jurídica.")
            Text("Duração da condenação", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = anos, onValueChange = { anos = it; resultado = null },
                    label = { Text("Anos") }, singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = meses, onValueChange = { meses = it; resultado = null },
                    label = { Text("Meses") }, singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = dias, onValueChange = { dias = it; resultado = null },
                    label = { Text("Dias") }, singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Regime informado", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Regime.entries.forEach { opcao ->
                    FilterChip(
                        selected = regime == opcao,
                        onClick = { regime = opcao; resultado = null },
                        label = { Text(opcao.descricao) }
                    )
                }
            }

            OutlinedTextField(
                value = inicio, onValueChange = { inicio = it; resultado = null },
                label = { Text("Data de início (AAAA-MM-DD)") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = referencia, onValueChange = { referencia = it; resultado = null },
                label = { Text("Data de referência (AAAA-MM-DD)") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    try {
                        val dados = DadosCondenacao(
                            anos = inteiro(anos, "Anos"),
                            meses = inteiro(meses, "Meses"),
                            dias = inteiro(dias, "Dias"),
                            regime = regime,
                            inicio = LocalDate.parse(inicio),
                            referencia = LocalDate.parse(referencia)
                        )
                        resultado = CalculadoraPena.calcular(dados)
                        erro = null
                    } catch (_: DateTimeParseException) {
                        resultado = null
                        erro = "Informe datas válidas no formato AAAA-MM-DD."
                    } catch (ex: IllegalArgumentException) {
                        resultado = null
                        erro = ex.message ?: "Verifique os dados informados."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calcular")
            }

            erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            resultado?.let {
                val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                Text("Resultado demonstrativo", style = MaterialTheme.typography.titleLarge)
                Text("Regime: ${regime.descricao}")
                Text("Término cronológico: ${it.terminoTeorico.format(formato)}")
                Text("Dias decorridos: ${it.diasDecorridos}")
                Text("Dias restantes: ${it.diasRestantes}")
            }

            OutlinedButton(
                onClick = {
                    anos = ""; meses = "0"; dias = "0"
                    regime = Regime.FECHADO
                    inicio = ""; referencia = LocalDate.now().toString()
                    resultado = null; erro = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Nova simulação")
            }

            Text(
                "A projeção pressupõe cumprimento ininterrupto. Não considera " +
                    "detração, remição, faltas graves ou regras de progressão de regime. " +
                    "Não utilize para definir direitos ou prazos processuais.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun inteiro(texto: String, campo: String): Int =
    texto.toIntOrNull() ?: throw IllegalArgumentException("$campo deve ser um número inteiro.")
