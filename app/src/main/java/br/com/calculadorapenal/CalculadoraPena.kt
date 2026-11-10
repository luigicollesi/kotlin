package br.com.calculadorapenal

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class Regime(val descricao: String) {
    FECHADO("Fechado"),
    SEMIABERTO("Semiaberto"),
    ABERTO("Aberto")
}

data class DadosCondenacao(
    val anos: Int,
    val meses: Int,
    val dias: Int,
    val regime: Regime,
    val inicio: LocalDate,
    val referencia: LocalDate
)

data class ResultadoPena(
    val terminoTeorico: LocalDate,
    val diasDecorridos: Long,
    val diasRestantes: Long
)

/**
 * Projeção cronológica didática: pressupõe cumprimento contínuo.
 * NÃO calcula progressão de regime ou saldo jurídico da execução penal.
 */
object CalculadoraPena {
    fun calcular(dados: DadosCondenacao): ResultadoPena {
        require(dados.anos >= 0) { "Anos não podem ser negativos." }
        require(dados.meses in 0..11) { "Meses devem estar entre 0 e 11." }
        require(dados.dias in 0..30) { "Dias devem estar entre 0 e 30." }
        require(dados.anos + dados.meses + dados.dias > 0) {
            "Informe uma duração maior que zero."
        }
        require(!dados.referencia.isBefore(dados.inicio)) {
            "A data de referência não pode ser anterior ao início."
        }

        val termino = dados.inicio.plusYears(dados.anos.toLong())
            .plusMonths(dados.meses.toLong())
            .plusDays(dados.dias.toLong())

        val decorridos = ChronoUnit.DAYS.between(dados.inicio, minOf(dados.referencia, termino))
        val restantes = ChronoUnit.DAYS.between(dados.referencia, termino).coerceAtLeast(0)

        return ResultadoPena(termino, decorridos, restantes)
    }
}
