package br.com.calculadorapenal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalculadoraPenaTest {
    private fun dados(
        fato: String = "2025-01-01",
        inicio: String = "2025-02-01",
        referencia: String = "2025-02-01",
        tipo: TipoCrime = TipoCrime.COMUM_SEM_VIOLENCIA,
        reincidente: Boolean = false,
        regime: Regime = Regime.FECHADO,
        confirmado: Boolean = true
    ) = DadosCondenacao(
        anos = 1, meses = 0, dias = 0, regime = regime, tipoCrime = tipo,
        reincidente = reincidente, dataFato = LocalDate.parse(fato),
        inicio = LocalDate.parse(inicio), referencia = LocalDate.parse(referencia),
        casoSimples = confirmado
    )

    @Test fun aplica16PorCentoAoFatoAnteriorA2026() {
        val r = CalculadoraPena.calcular(dados())
        assertEquals("16%", r.regra.descricao)
        assertEquals(59L, r.requisitoDias)
        assertEquals(LocalDate.of(2025, 3, 31), r.dataProgressao)
    }

    @Test fun aplicaUmSextoSomenteAFatosDesdeMaio2026() {
        val r = CalculadoraPena.calcular(dados("2026-05-08", "2026-06-01", "2026-06-01"))
        assertEquals(61L, r.requisitoDias)
        assertEquals("1/6 (aprox. 16,67%)", r.regra.descricao)
        assertEquals(LocalDate.of(2026, 7, 31), r.dataProgressao)
    }

    @Test fun naoRetroageUmSextoMaisGravoso() {
        val r = CalculadoraPena.calcular(dados("2026-05-07", "2026-06-01", "2026-06-01"))
        assertEquals("16%", r.regra.descricao)
    }

    @Test fun calculaDemaisFracoesComuns() {
        val recid = CalculadoraPena.calcular(dados(reincidente = true))
        val viol = CalculadoraPena.calcular(dados(tipo = TipoCrime.COMUM_COM_VIOLENCIA))
        val violRecid = CalculadoraPena.calcular(dados(
            tipo = TipoCrime.COMUM_COM_VIOLENCIA, reincidente = true
        ))
        assertEquals(73L, recid.requisitoDias)
        assertEquals(92L, viol.requisitoDias)
        assertEquals(110L, violRecid.requisitoDias)
    }

    @Test fun computaDiaInicialEImpedeSaldoNegativo() {
        val r = CalculadoraPena.calcular(dados(referencia = "2027-03-01"))
        assertEquals(365L, r.diasCumpridos)
        assertEquals(0L, r.diasRestantes)
        assertEquals(0L, r.diasParaProgressao)
    }

    @Test fun progressaoDoSemiabertoApontaParaAberto() {
        val r = CalculadoraPena.calcular(dados(regime = Regime.SEMIABERTO))
        assertEquals(Regime.ABERTO, r.regimeSeguinte)
    }

    @Test fun naoCalculaCrimeEspecial() {
        val erro = runCatching {
            CalculadoraPena.calcular(dados(tipo = TipoCrime.ESPECIAL))
        }.exceptionOrNull()
        assertTrue(erro is IllegalArgumentException)
    }

    @Test(expected = IllegalArgumentException::class)
    fun naoCalculaCasoComIncidentes() {
        CalculadoraPena.calcular(dados(confirmado = false))
    }

    @Test(expected = IllegalArgumentException::class)
    fun naoCalculaRegimeAberto() {
        CalculadoraPena.calcular(dados(regime = Regime.ABERTO))
    }

    @Test(expected = IllegalArgumentException::class)
    fun naoCalculaFatoAnteriorA2020() {
        CalculadoraPena.calcular(dados(fato = "2019-12-01"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun naoAceitaDataBasePosteriorAReferencia() {
        CalculadoraPena.calcular(dados(referencia = "2025-01-01"))
    }
