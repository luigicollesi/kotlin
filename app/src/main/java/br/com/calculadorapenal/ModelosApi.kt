package br.com.calculadorapenal

import java.time.LocalDate

/** Modelos para envio dos dados e apresentação do resultado no Android. */
enum class Regime(val descricao: String) {
    FECHADO("Fechado"),
    SEMIABERTO("Semiaberto"),
    ABERTO("Aberto")
}

enum class TipoCrime(val descricao: String) {
    COMUM_SEM_VIOLENCIA("Comum, sem violência ou grave ameaça"),
    COMUM_COM_VIOLENCIA("Comum, com violência ou grave ameaça"),
    ESPECIAL("Hediondo, equiparado ou outra hipótese especial")
}

data class DadosCondenacao(
    val anos: Int,
    val meses: Int,
    val dias: Int,
    val regime: Regime,
    val tipoCrime: TipoCrime,
    val reincidente: Boolean,
    val dataFato: LocalDate,
    val inicio: LocalDate,
    val referencia: LocalDate,
    val casoSimples: Boolean
)

data class RegraProgressao(val descricao: String, val fundamento: String)

data class ResultadoPena(
    val regimeSeguinte: Regime,
    val regra: RegraProgressao,
    val totalDias: Long,
    val requisitoDias: Long,
    val diasCumpridos: Long,
    val diasRestantes: Long,
    val diasParaProgressao: Long,
    val dataProgressao: LocalDate,
    val terminoTeorico: LocalDate
)
