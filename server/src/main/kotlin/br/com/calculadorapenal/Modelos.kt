package br.com.calculadorapenal

import kotlinx.serialization.Serializable
import java.time.LocalDate

/** Contrato de entrada da API. As datas usam o formato ISO AAAA-MM-DD. */
@Serializable
data class PedidoCalculo(
    val anos: Int,
    val meses: Int,
    val dias: Int,
    val regime: String,
    val tipoCrime: String,
    val reincidencia: String,
    val dataFato: String,
    val inicio: String,
    val referencia: String,
    val casoSimples: Boolean
) {
    fun paraDados() = DadosCondenacao(
        anos = anos,
        meses = meses,
        dias = dias,
        regime = Regime.entries.firstOrNull { it.name == regime }
            ?: throw IllegalArgumentException("Informe um regime válido."),
        tipoCrime = TipoCrime.entries.firstOrNull { it.name == tipoCrime }
            ?: throw IllegalArgumentException("Informe uma classificação de crime válida."),
        reincidencia = Reincidencia.entries.firstOrNull { it.name == reincidencia }
            ?: throw IllegalArgumentException("Informe uma situação de reincidência válida."),
        dataFato = LocalDate.parse(dataFato),
        inicio = LocalDate.parse(inicio),
        referencia = LocalDate.parse(referencia),
        casoSimples = casoSimples
    )
}

@Serializable
data class RespostaErro(val erro: String)

@Serializable
data class RespostaSaude(val status: String)

@Serializable
data class RegraApi(val descricao: String, val fundamento: String)

@Serializable
data class RespostaCalculo(
    val regimeSeguinte: String,
    val regra: RegraApi,
    val totalDias: Long,
    val requisitoDias: Long,
    val diasCumpridos: Long,
    val diasRestantes: Long,
    val diasParaProgressao: Long,
    val dataProgressao: String,
    val terminoTeorico: String
)

fun ResultadoPena.paraResposta() = RespostaCalculo(
    regimeSeguinte = regimeSeguinte.name,
    regra = RegraApi(regra.descricao, regra.fundamento),
    totalDias = totalDias,
    requisitoDias = requisitoDias,
    diasCumpridos = diasCumpridos,
    diasRestantes = diasRestantes,
    diasParaProgressao = diasParaProgressao,
    dataProgressao = dataProgressao.toString(),
    terminoTeorico = terminoTeorico.toString()
)
