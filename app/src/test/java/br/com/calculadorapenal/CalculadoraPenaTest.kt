package br.com.calculadorapenal

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CalculadoraPenaTest {
    private fun dados(inicio: String, referencia: String, anos: Int = 1) = DadosCondenacao(
        anos = anos,
        meses = 0,
        dias = 0,
        regime = Regime.FECHADO,
        inicio = LocalDate.parse(inicio),
        referencia = LocalDate.parse(referencia)
    )

    @Test
    fun calculaDiasDecorridosERestantes() {
        val resultado = CalculadoraPena.calcular(dados("2025-01-01", "2025-07-01"))
        assertEquals(LocalDate.parse("2026-01-01"), resultado.terminoTeorico)
        assertEquals(181L, resultado.diasDecorridos)
        assertEquals(184L, resultado.diasRestantes)
    }

    @Test
    fun naoRetornaSaldoNegativoAposTermino() {
        val resultado = CalculadoraPena.calcular(dados("2025-01-01", "2026-03-01"))
        assertEquals(0L, resultado.diasRestantes)
        assertEquals(365L, resultado.diasDecorridos)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejeitaReferenciaAnteriorAoInicio() {
        CalculadoraPena.calcular(dados("2025-01-01", "2024-12-31"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejeitaDuracaoZerada() {
        CalculadoraPena.calcular(dados("2025-01-01", "2025-01-01", anos = 0))
    }
}
