package br.com.calculadorapenal

import java.time.LocalDate

data class RegraProgressao(
    val numerador: Int,
    val denominador: Int,
    val descricao: String,
    val fundamento: String
)

/**
 * Recorte didático: uma pena em execução, crimes comuns e primeira progressão.
 * Fatos antes de maio/2026 usam a redação anterior mais favorável.
 * Em crime comum violento, a reincidência genérica anterior à lei de 2026
 * recebe 25% conforme entendimento do STJ; não aplicar 30% indistintamente.
 */
object RegrasProgressao {
    private val marco2019 = LocalDate.of(2020, 1, 23)
    private val marco2026 = LocalDate.of(2026, 5, 8)

    fun selecionar(dados: DadosCondenacao): RegraProgressao {
        if (dados.dataFato.isBefore(marco2019)) {
            throw CasoNaoSuportadoException("Fatos anteriores a 23/01/2020 não são suportados.")
        }
        if (dados.tipoCrime == TipoCrime.ESPECIAL) {
            throw CasoNaoSuportadoException(
                "Crimes hediondos, equiparados e outras hipóteses especiais não são suportados."
            )
        }
        if (dados.reincidencia == Reincidencia.NAO_INFORMADO) {
            throw CasoNaoSuportadoException(
                "Confirme a situação de reincidência reconhecida para este cálculo."
            )
        }

        val lei2026 = !dados.dataFato.isBefore(marco2026)
        val fundamento2019 = "LEP, art. 112, redação da Lei 13.964/2019"
        val fundamento2026 = "LEP, art. 112, redação da Lei 15.402/2026"
        val primario = dados.reincidencia == Reincidencia.PRIMARIO
        val violento = dados.tipoCrime == TipoCrime.COMUM_COM_VIOLENCIA

        return when {
            !violento && primario && !lei2026 ->
                RegraProgressao(16, 100, "16%", "$fundamento2019, inciso I (lei mais favorável)")
            !violento && primario ->
                RegraProgressao(1, 6, "1/6 (aprox. 16,67%)", "$fundamento2026, caput")
            !violento ->
                RegraProgressao(20, 100, "20%", if (lei2026) "$fundamento2026, inciso III"
                    else "$fundamento2019, inciso II")
            violento && primario ->
                RegraProgressao(25, 100, "25%", if (lei2026) "$fundamento2026, inciso I"
                    else "$fundamento2019, inciso III")
            !lei2026 && dados.reincidencia == Reincidencia.ANTERIOR_SEM_VIOLENCIA ->
                RegraProgressao(
                    25, 100, "25%",
                    "$fundamento2019, inciso III (analogia favorável; jurisprudência do STJ)"
                )
            else ->
                RegraProgressao(30, 100, "30%", if (lei2026) "$fundamento2026, inciso II"
                    else "$fundamento2019, inciso IV")
        }
    }
}
