package br.com.calculadorapenal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private enum class CategoriaCrime { COMUM, ESPECIAL, NAO_SEI }
private enum class ViolenciaCrime { NAO, SIM, NAO_SEI }

@Composable
fun TelaCalculadora() {
    var anos by remember { mutableStateOf("") }
    var meses by remember { mutableStateOf("0") }
    var dias by remember { mutableStateOf("0") }
    var regime by remember { mutableStateOf(Regime.FECHADO) }
    var categoria by remember { mutableStateOf<CategoriaCrime?>(null) }
    var violencia by remember { mutableStateOf<ViolenciaCrime?>(null) }
    var reincidencia by remember { mutableStateOf<Reincidencia?>(null) }
    var dataFato by remember { mutableStateOf("") }
    var inicio by remember { mutableStateOf("") }
    var referencia by remember { mutableStateOf(LocalDate.now().toString()) }
    var casoSimples by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<ResultadoPena?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var calculando by remember { mutableStateOf(false) }
    var revisaoFormulario by remember { mutableIntStateOf(0) }
    val escopo = rememberCoroutineScope()
    val rolagem = rememberScrollState()

    fun invalidar() {
        resultado = null
        erro = null
        revisaoFormulario++
    }

    LaunchedEffect(resultado, erro) {
        if (resultado != null || erro != null) {
            delay(if (resultado != null) 260 else 60)
            rolagem.animateScrollTo(rolagem.maxValue)
        }
    }

    Scaffold(containerColor = FundoClaro) { margem ->
        Column(
            Modifier.fillMaxSize().padding(margem).verticalScroll(rolagem).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("CALCULADORA PENAL", color = TextoSecundario,
                style = MaterialTheme.typography.labelMedium)
            Text("Progressão de regime", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Estimativa acadêmica do tempo mínimo para a primeira progressão.",
                color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)

            Secao("01  Dados da condenação") {
                Text("Duração da pena", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoNumero("Anos", anos, { anos = it; invalidar() }, Modifier.weight(1f))
                    CampoNumero("Meses", meses, { meses = it; invalidar() }, Modifier.weight(1f))
                    CampoNumero("Dias", dias, { dias = it; invalidar() }, Modifier.weight(1f))
                }
                Text("Regime inicial da pena", style = MaterialTheme.typography.labelLarge)
                Text("Selecione o regime inicial, antes de qualquer progressão.",
                    color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
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

            Secao("02  Características do crime") {
                Text("O crime é hediondo, equiparado ou se enquadra em outra hipótese especial?",
                    style = MaterialTheme.typography.labelLarge)
                Text(
                    "A classificação depende da lei e da condenação, não apenas da gravidade percebida.",
                    color = TextoSecundario, style = MaterialTheme.typography.bodySmall
                )
                OpcaoRadio(categoria == CategoriaCrime.COMUM, "Não, é crime comum") {
                    categoria = CategoriaCrime.COMUM; invalidar()
                }
                OpcaoRadio(categoria == CategoriaCrime.ESPECIAL,
                    "Sim, é hediondo, equiparado ou hipótese especial") {
                    categoria = CategoriaCrime.ESPECIAL; violencia = null; invalidar()
                }
                OpcaoRadio(categoria == CategoriaCrime.NAO_SEI, "Não sei informar") {
                    categoria = CategoriaCrime.NAO_SEI; violencia = null; invalidar()
                }

                if (categoria == CategoriaCrime.COMUM) {
                    HorizontalDivider()
                    Text("O crime envolveu violência contra pessoa ou grave ameaça?",
                        style = MaterialTheme.typography.labelLarge)
                    Text("Utilize o enquadramento jurídico do caso, não apenas o relato do ocorrido.",
                        color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                    OpcaoRadio(violencia == ViolenciaCrime.NAO,
                        "Não houve violência contra pessoa nem grave ameaça") {
                        violencia = ViolenciaCrime.NAO; invalidar()
                    }
                    OpcaoRadio(violencia == ViolenciaCrime.SIM,
                        "Houve violência contra pessoa ou grave ameaça") {
                        violencia = ViolenciaCrime.SIM; invalidar()
                    }
                    OpcaoRadio(violencia == ViolenciaCrime.NAO_SEI, "Não sei informar") {
                        violencia = ViolenciaCrime.NAO_SEI; invalidar()
                    }
                } else if (categoria != null) {
                    Text("Este caso não pode ser calculado nesta versão. Confira a classificação " +
                        "jurídica antes de prosseguir.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }

                HorizontalDivider()
                Text("Existe reincidência reconhecida para este cálculo?",
                    style = MaterialTheme.typography.labelLarge)
                Text("Se houver reincidência, informe se o crime anterior envolveu violência " +
                    "contra pessoa ou grave ameaça.",
                    color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                Reincidencia.entries.forEach { opcao ->
                    OpcaoRadio(reincidencia == opcao, opcao.descricao) {
                        reincidencia = opcao; invalidar()
                    }
                }
                if (reincidencia == Reincidencia.NAO_INFORMADO) {
                    Text("É necessário confirmar a reincidência no caso para realizar o cálculo.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            Secao("03  Datas e condições") {
                CampoData("Data do fato", dataFato) { dataFato = it; invalidar() }
                Text("Quando o crime foi cometido. Define qual redação legal será considerada.",
                    color = TextoSecundario, style = MaterialTheme.typography.bodySmall)

                CampoData("Data-base para o cálculo", inicio) { inicio = it; invalidar() }
                Text("Marco inicial da contagem adotado na execução penal. Não é necessariamente " +
                    "a data da sentença.",
                    color = TextoSecundario, style = MaterialTheme.typography.bodySmall)

                CampoData("Calcular situação em", referencia) { referencia = it; invalidar() }
                Text("Data em que deseja verificar quantos dias do requisito ainda faltam. " +
                    "Por padrão, é a data atual.",
                    color = TextoSecundario, style = MaterialTheme.typography.bodySmall)

                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = casoSimples,
                        onCheckedChange = { casoSimples = it; invalidar() }
                    )
                    Text(
                        "Confirmo que há apenas esta pena em execução, sem remição, detração, " +
                            "falta grave, interrupção ou progressão anterior, e sem condição especial.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                Text("Se houver qualquer uma dessas situações, este simulador acadêmico " +
                    "não representa adequadamente o caso.",
                    color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                enabled = !calculando,
                onClick = {
                    val dados = try {
                        val classificacao = requireNotNull(categoria) {
                            "Informe se o crime é comum ou se possui classificação especial."
                        }
                        require(classificacao == CategoriaCrime.COMUM) {
                            "Este caso não é suportado. Consulte a classificação jurídica."
                        }
                        val houveViolencia = requireNotNull(violencia) {
                            "Informe se o crime envolveu violência ou grave ameaça."
                        }
                        require(houveViolencia != ViolenciaCrime.NAO_SEI) {
                            "Confirme se o crime envolveu violência ou grave ameaça."
                        }
                        val situacaoReincidencia = requireNotNull(reincidencia) {
                            "Selecione a situação de reincidência."
                        }
                        require(situacaoReincidencia != Reincidencia.NAO_INFORMADO) {
                            "Confirme a situação de reincidência antes de calcular."
                        }
                        require(casoSimples) {
                            "Confirme as condições da simulação na seção 03."
                        }
                        DadosCondenacao(
                            anos = requireNotNull(anos.toIntOrNull()) { "Informe os anos." },
                            meses = requireNotNull(meses.toIntOrNull()) { "Informe os meses." },
                            dias = requireNotNull(dias.toIntOrNull()) { "Informe os dias." },
                            regime = regime,
                            tipoCrime = if (houveViolencia == ViolenciaCrime.SIM)
                                TipoCrime.COMUM_COM_VIOLENCIA else TipoCrime.COMUM_SEM_VIOLENCIA,
                            reincidencia = situacaoReincidencia,
                            dataFato = LocalDate.parse(dataFato),
                            inicio = LocalDate.parse(inicio),
                            referencia = LocalDate.parse(referencia),
                            casoSimples = casoSimples
                        )
                    } catch (_: DateTimeException) {
                        erro = "Selecione as três datas válidas."
                        null
                    } catch (e: IllegalArgumentException) {
                        erro = e.message ?: "Confira os dados."
                        null
                    }

                    if (dados != null) {
                        val revisaoEnviada = revisaoFormulario
                        calculando = true
                        erro = null
                        resultado = null
                        escopo.launch {
                            try {
                                val resposta = ApiClient.calcular(dados)
                                if (revisaoFormulario == revisaoEnviada) {
                                    resultado = resposta
                                }
                            } catch (e: ErroInternoServidorException) {
                                if (revisaoFormulario == revisaoEnviada) {
                                    erro = "O servidor encontrou um erro ao calcular. Tente novamente."
                                }
                            } catch (e: RespostaInvalidaException) {
                                if (revisaoFormulario == revisaoEnviada) {
                                    erro = "O servidor retornou um resultado inválido."
                                }
                            } catch (_: IOException) {
                                if (revisaoFormulario == revisaoEnviada) {
                                    erro = "Não foi possível conectar ao servidor local. " +
                                        "Verifique se ./gradlew :server:run está ativo."
                                }
                            } catch (e: IllegalArgumentException) {
                                if (revisaoFormulario == revisaoEnviada) {
                                    erro = e.message ?: "Dados inválidos."
                                }
                            } finally {
                                calculando = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                Text(if (calculando) "Calculando..." else "Calcular requisito temporal")
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
                        Text("Data estimada para completar o tempo mínimo",
                            color = TextoSecundario,
                            style = MaterialTheme.typography.labelLarge)
                        Text(r.dataProgressao.format(formato),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold)
                        Text(regime.descricao + " → " + r.regimeSeguinte.descricao)
                        if (r.diasParaProgressao == 0L) {
                            Text("Requisito temporal já atingido na data informada.",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        HorizontalDivider()
                        Text("Fração aplicada: " + r.regra.descricao)
                        Text("Tempo mínimo exigido: " + r.requisitoDias + " dias")
                        Text("Dias restantes para cumprir o requisito: " + r.diasParaProgressao)
                        Text("Dias restantes da pena (estimativa): " + r.diasRestantes)
                        Text("Término cronológico estimado da pena: " +
                            r.terminoTeorico.format(formato))
                        HorizontalDivider()
                        Text("Fundamentação jurídica", fontWeight = FontWeight.SemiBold)
                        Text(r.regra.fundamento,
                            style = MaterialTheme.typography.bodySmall)
                        Text(
                            "A data indica apenas o requisito temporal estimado. A progressão " +
                                "exige outros requisitos legais e decisão judicial. " +
                                "Contagem didática: ano = 365 dias, mês = 30 dias e início incluso.",
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
                    categoria = null; violencia = null; reincidencia = null
                    casoSimples = false
                    dataFato = ""; inicio = ""
                    referencia = LocalDate.now().toString()
                    invalidar()
                    escopo.launch { rolagem.animateScrollTo(0) }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("Nova simulação")
            }

            Text(
                "Uso acadêmico. Apenas crimes comuns, fatos desde 23/01/2020 e " +
                    "primeira progressão sem incidentes. Não substitui cálculo " +
                    "judicial nem análise jurídica.",
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
private fun OpcaoRadio(selecionado: Boolean, titulo: String, aoClicar: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = aoClicar),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selecionado, onClick = aoClicar)
        Text(titulo, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
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
            DatePicker(state = calendario, showModeToggle = true)
        }
    }
}
