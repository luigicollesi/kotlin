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
        reincidencia: Reincidencia = Reincidencia.PRIMARIO,
        regime: Regime = Regime.FECHADO,
        confirmado: Boolean = true
    ) = DadosCondenacao(
        anos = 1, meses = 0, dias = 0, regime = regime, tipoCrime = tipo,
        reincidencia = reincidencia, dataFato = LocalDate.parse(fato),
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
        val recid = CalculadoraPena.calcular(dados(reincidencia = Reincidencia.ANTERIOR_SEM_VIOLENCIA))
        val viol = CalculadoraPena.calcular(dados(tipo = TipoCrime.COMUM_COM_VIOLENCIA))
        val violRecid = CalculadoraPena.calcular(dados(
            tipo = TipoCrime.COMUM_COM_VIOLENCIA, reincidencia = Reincidencia.ANTERIOR_COM_VIOLENCIA
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

    @Test fun marcoDaNovaLeiNaoAlteraFatoDoDiaAnterior() {
        val anterior = CalculadoraPena.calcular(
            dados("2026-05-07", "2026-06-01", "2026-06-01")
        )
        val posterior = CalculadoraPena.calcular(
            dados("2026-05-08", "2026-06-01", "2026-06-01")
        )
        assertEquals(59L, anterior.requisitoDias)
        assertEquals(61L, posterior.requisitoDias)
    }

    @Test(expected = IllegalArgumentException::class)
    fun naoAceitaFatoPosteriorADataBase() {
        CalculadoraPena.calcular(dados(fato = "2025-03-01", inicio = "2025-02-01"))
    }
    @Test fun reincidenciaGenericaViolentaAntesDeMaio2026Aplica25() {
        val r = CalculadoraPena.calcular(
            dados(tipo = TipoCrime.COMUM_COM_VIOLENCIA,
                reincidencia = Reincidencia.ANTERIOR_SEM_VIOLENCIA)
        )
        assertEquals(92L, r.requisitoDias)
        assertEquals("25%", r.regra.descricao)
        assertTrue(r.regra.fundamento.contains("STJ"))
    }

    @Test fun reincidenciaEspecificaViolentaAntesDeMaio2026Aplica30() {
        val r = CalculadoraPena.calcular(
            dados(tipo = TipoCrime.COMUM_COM_VIOLENCIA,
                reincidencia = Reincidencia.ANTERIOR_COM_VIOLENCIA)
        )
        assertEquals(110L, r.requisitoDias)
    }

    @Test fun reincidenciaGenericaViolentaDepoisDeMaio2026Aplica30() {
        val r = CalculadoraPena.calcular(
            dados(fato = "2026-05-08", inicio = "2026-06-01", referencia = "2026-06-01",
                tipo = TipoCrime.COMUM_COM_VIOLENCIA,
                reincidencia = Reincidencia.ANTERIOR_SEM_VIOLENCIA)
        )
        assertEquals(110L, r.requisitoDias)
        assertTrue(r.regra.fundamento.contains("15.402/2026"))
    }

    @Test fun reincidenciaEmCrimeNaoViolentoAplica20NasDuasSituacoes() {
        for (situacao in listOf(
            Reincidencia.ANTERIOR_SEM_VIOLENCIA, Reincidencia.ANTERIOR_COM_VIOLENCIA
        )) {
            assertEquals(73L, CalculadoraPena.calcular(dados(reincidencia = situacao)).requisitoDias)
        }
    }

    @Test(expected = CasoNaoSuportadoException::class)
    fun recusaReincidenciaNaoInformada() {
        CalculadoraPena.calcular(dados(reincidencia = Reincidencia.NAO_INFORMADO))
    }

    @Test fun contaDiasCorridosEmAnoBissextoComConvencaoDe365Dias() {
        val r = CalculadoraPena.calcular(dados(fato = "2024-01-01",
            inicio = "2024-02-28", referencia = "2024-02-29"))
        assertEquals(2L, r.diasCumpridos)
        assertEquals(365L, r.totalDias)
        assertEquals(363L, r.diasRestantes)
    }

}
