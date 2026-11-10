package br.com.calculadorapenal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun TelaCalculadora() {
    var anos by remember { mutableStateOf("") }
    var meses by remember { mutableStateOf("0") }
    var dias by remember { mutableStateOf("0") }
    var regime by remember { mutableStateOf(Regime.FECHADO) }
    var tipo by remember { mutableStateOf(TipoCrime.COMUM_SEM_VIOLENCIA) }
    var reincidente by remember { mutableStateOf(false) }
    var dataFato by remember { mutableStateOf("") }
    var inicio by remember { mutableStateOf("") }
    var referencia by remember { mutableStateOf(LocalDate.now().toString()) }
    var casoSimples by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<ResultadoPena?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }

    fun invalidar() { resultado = null; erro = null }

    Scaffold(containerColor = FundoClaro) { margem ->
        Column(
            Modifier.fillMaxSize().padding(margem).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("CALCULADORA PENAL", color = TextoSecundario,
                style = MaterialTheme.typography.labelMedium)
            Text("Progressão de regime", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Simulação acadêmica do requisito temporal da primeira progressão.",
                color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)

            Secao("01  Dados da condenação") {
                Text("Duração da pena", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoNumero("Anos", anos, { anos = it; invalidar() }, Modifier.weight(1f))
                    CampoNumero("Meses", meses, { meses = it; invalidar() }, Modifier.weight(1f))
                    CampoNumero("Dias", dias, { dias = it; invalidar() }, Modifier.weight(1f))
                }
                Text("Regime inicial (sem progressão anterior)", style = MaterialTheme.typography.labelLarge)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Regime.entries.forEach { opcao ->
                        FilterChip(
                            selected = regime == opcao,
                            onClick = { regime = opcao; invalidar() },
                            label = { Text(opcao.descricao) }
                        )
                    }
                }
            }

            Secao("02  Enquadramento jurídico") {
                Text("Classificação do crime", style = MaterialTheme.typography.labelLarge)
                TipoCrime.entries.forEach { opcao ->
                    Row(
                        Modifier.fillMaxWidth().clickable { tipo = opcao; invalidar() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipo == opcao,
                            onClick = { tipo = opcao; invalidar() }
                        )
                        Text(opcao.descricao, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reincidente", modifier = Modifier.weight(1f))
                    Switch(
                        checked = reincidente,
                        onCheckedChange = { reincidente = it; invalidar() }
                    )
                }
            }

            Secao("03  Datas e condições") {
                CampoData("Data do fato", dataFato) { dataFato = it; invalidar() }
                CampoData("Data-base do cumprimento", inicio) { inicio = it; invalidar() }
                CampoData("Data de referência", referencia) { referencia = it; invalidar() }
                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = casoSimples,
                        onCheckedChange = { casoSimples = it; invalidar() }
                    )
                    Text(
                        "Confirmo que é uma condenação única, sem falta grave, interrupção, " +
                            "detração, remição ou progressão anterior; não há hipótese especial " +
                            "(crimes do Título XII do Código Penal ou art. 112, § 3º).",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    try {
                        val dados = DadosCondenacao(
                            anos = requireNotNull(anos.toIntOrNull()) { "Informe os anos." },
                            meses = requireNotNull(meses.toIntOrNull()) { "Informe os meses." },
                            dias = requireNotNull(dias.toIntOrNull()) { "Informe os dias." },
                            regime = regime,
                            tipoCrime = tipo,
                            reincidente = reincidente,
                            dataFato = LocalDate.parse(dataFato),
                            inicio = LocalDate.parse(inicio),
                            referencia = LocalDate.parse(referencia),
                            casoSimples = casoSimples
                        )
                        resultado = CalculadoraPena.calcular(dados)
                        erro = null
                    } catch (e: DateTimeException) {
                        resultado = null
                        erro = "Selecione as três datas válidas."
                    } catch (e: IllegalArgumentException) {
                        resultado = null
                        erro = e.message ?: "Confira os dados."
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                Text("Calcular progressão")
            }

            erro?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium)
            }

            AnimatedVisibility(
                visible = resultado != null,
                enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 10 },
                exit = fadeOut(tween(120))
            ) {
                resultado?.let { r ->
                    Secao("Resultado da simulação") {
                        Text("Requisito temporal de progressão",
                            color = TextoSecundario,
                            style = MaterialTheme.typography.labelLarge)
                        Text(r.dataProgressao.format(formato),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold)
                        Text(regime.descricao + " → " + r.regimeSeguinte.descricao)
                        HorizontalDivider()
                        Text("Fração aplicada: " + r.regra.descricao)
                        Text("Tempo exigido: " + r.requisitoDias + " dias")
                        Text("Dias restantes até o requisito: " + r.diasParaProgressao)
                        Text("Saldo cronológico estimado da pena: " + r.diasRestantes + " dias")
                        Text("Término cronológico estimado: " +
                            r.terminoTeorico.format(formato))
                        HorizontalDivider()
                        Text("Fundamentação", fontWeight = FontWeight.SemiBold)
                        Text(r.regra.fundamento,
                            style = MaterialTheme.typography.bodySmall)
                        Text(
                            "A data não concede automaticamente progressão. Requisitos " +
                                "subjetivos e decisão judicial devem ser verificados. " +
                                "Convenção: ano = 365 dias, mês = 30 dias, início incluso.",
                            color = TextoSecundario,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    anos = ""; meses = "0"; dias = "0"
                    regime = Regime.FECHADO
                    tipo = TipoCrime.COMUM_SEM_VIOLENCIA
                    reincidente = false; casoSimples = false
                    dataFato = ""; inicio = ""
                    referencia = LocalDate.now().toString()
                    invalidar()
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("Nova simulação")
            }

            Text(
                "Uso acadêmico. Cálculo limitado a crimes comuns, fatos desde " +
                    "23/01/2020 e primeira progressão sem incidentes. Não substitui " +
                    "o cálculo judicial nem a análise jurídica.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )
        }
    }
}

@Composable
private fun Secao(titulo: String, conteudo: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium)
            conteudo()
        }
    }
}

@Composable
private fun CampoNumero(
    nome: String,
    valor: String,
    aoAlterar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
            .background(Color.White).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(nome, color = TextoSecundario, style = MaterialTheme.typography.labelSmall)
        BasicTextField(
            value = valor,
            onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) aoAlterar(it) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(fontSize = 18.sp, color = AzulProfundo),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoData(nome: String, valor: String, aoAlterar: (String) -> Unit) {
    var aberto by remember { mutableStateOf(false) }
    val exibicao = valor.takeIf { it.isNotBlank() }?.let {
        runCatching { LocalDate.parse(it).format(formato) }.getOrDefault(it)
    } ?: "Selecionar data"
    OutlinedButton(
        onClick = { aberto = true },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
    ) {
        Text(nome + ": " + exibicao)
    }

    if (aberto) {
        val inicial = runCatching {
            LocalDate.parse(valor).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
        val calendario = rememberDatePickerState(initialSelectedDateMillis = inicial)
        DatePickerDialog(
            onDismissRequest = { aberto = false },
            confirmButton = {
                TextButton(onClick = {
                    calendario.selectedDateMillis?.let {
                        aoAlterar(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC)
                            .toLocalDate().toString())
                    }
                    aberto = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { aberto = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = calendario)
        }
    }
}
