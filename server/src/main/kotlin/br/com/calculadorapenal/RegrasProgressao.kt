package br.com.calculadorapenal

import java.time.LocalDate

data class RegraProgressao(
    val numerador: Int,
    val denominador: Int,
    val descricao: String,
    val fundamento: String
)

/**
 * Recorte acadêmico: crimes comuns sem hipóteses especiais, fatos desde
 * 23/01/2020. A lei mais gravosa de 2026 não retroage para fatos anteriores.
 * Casos fora desse recorte devem ser analisados individualmente.
 */
object RegrasProgressao {
    private val marco2019 = LocalDate.of(2020, 1, 23)
    private val marco2026 = LocalDate.of(2026, 5, 8)

    fun selecionar(dados: DadosCondenacao): RegraProgressao {
        if (dados.dataFato.isBefore(marco2019)) {
            throw CasoNaoSuportadoException("Fatos anteriores a 23/01/2020 ainda não são suportados.")
        }
        if (dados.tipoCrime == TipoCrime.ESPECIAL) {
            throw CasoNaoSuportadoException(
                "Crimes hediondos, equiparados e hipóteses especiais não são suportados."
            )
        }

        val lei2026 = !dados.dataFato.isBefore(marco2026)
        val fundamento2019 = "LEP, art. 112, redação da Lei 13.964/2019"
        val fundamento2026 = "LEP, art. 112, redação da Lei 15.402/2026"

        return when {
            dados.tipoCrime == TipoCrime.COMUM_SEM_VIOLENCIA && !dados.reincidente && !lei2026 ->
                RegraProgressao(16, 100, "16%", "$fundamento2019, inciso I (lei mais favorável)")
            dados.tipoCrime == TipoCrime.COMUM_SEM_VIOLENCIA && !dados.reincidente ->
                RegraProgressao(1, 6, "1/6 (aprox. 16,67%)", "$fundamento2026, caput")
            dados.tipoCrime == TipoCrime.COMUM_SEM_VIOLENCIA && dados.reincidente ->
                RegraProgressao(20, 100, "20%", if (lei2026) "$fundamento2026, inciso III"
                    else "$fundamento2019, inciso II")
            dados.tipoCrime == TipoCrime.COMUM_COM_VIOLENCIA && !dados.reincidente ->
                RegraProgressao(25, 100, "25%", if (lei2026) "$fundamento2026, inciso I"
                    else "$fundamento2019, inciso III")
            else ->
                RegraProgressao(30, 100, "30%", if (lei2026) "$fundamento2026, inciso II"
                    else "$fundamento2019, inciso IV")
        }
    }
}
