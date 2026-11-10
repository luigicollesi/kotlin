package br.com.calculadorapenal

import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

/**
 * Cálculo didático do requisito OBJETIVO da primeira progressão.
 * Pressupõe pena única, execução contínua e ausência de incidentes.
 * Anos = 365 dias; meses = 30 dias (convenção explicada na cartilha do CNJ).
 * O dia inicial conta como cumprido; a fração é arredondada para cima em dias.
 */
object CalculadoraPena {
    fun calcular(dados: DadosCondenacao): ResultadoPena {
        require(dados.anos in 0..100) { "Anos devem estar entre 0 e 100." }
        require(dados.meses in 0..11) { "Meses devem estar entre 0 e 11." }
        require(dados.dias in 0..30) { "Dias devem estar entre 0 e 30." }
        require(dados.anos + dados.meses + dados.dias > 0) { "Informe uma pena maior que zero." }
        if (dados.regime == Regime.ABERTO) {
            throw CasoNaoSuportadoException("Não há próxima progressão a partir do regime aberto.")
        }
        if (!dados.casoSimples) {
            throw CasoNaoSuportadoException(
                "Este cálculo só atende condenação única sem incidentes, detração ou remição."
            )
        }
        require(!dados.dataFato.isAfter(dados.inicio)) {
            "A data do fato não pode ser posterior à data-base."
        }
        require(!dados.referencia.isBefore(dados.inicio)) {
            "A data de referência não pode ser anterior à data-base."
        }

        val regra = RegrasProgressao.selecionar(dados)
        val total = dados.anos * 365L + dados.meses * 30L + dados.dias
        // Teto da fração: não antecipar o requisito temporal por parte de um dia.
        val requisito = (total * regra.numerador + regra.denominador - 1) / regra.denominador
        val cumpridos = (ChronoUnit.DAYS.between(dados.inicio, dados.referencia) + 1)
            .coerceIn(0L, total)

        return ResultadoPena(
            regimeSeguinte = if (dados.regime == Regime.FECHADO) Regime.SEMIABERTO else Regime.ABERTO,
            regra = regra,
            totalDias = total,
            requisitoDias = requisito,
            diasCumpridos = cumpridos,
            diasRestantes = total - cumpridos,
            diasParaProgressao = (requisito - cumpridos).coerceAtLeast(0),
            dataProgressao = dados.inicio.plusDays(requisito - 1),
            terminoTeorico = dados.inicio.plusDays(total - 1)
        )
    }
}
