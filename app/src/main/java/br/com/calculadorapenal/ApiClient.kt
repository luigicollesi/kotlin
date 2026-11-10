package br.com.calculadorapenal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

/**
 * Cliente HTTP mínimo para o emulador Android.
 * 10.0.2.2 acessa o localhost do computador que executa o Ktor.
 */
object ApiClient {
    private const val ENDERECO = "http://10.0.2.2:8080/api/calcular"

    suspend fun calcular(dados: DadosCondenacao): ResultadoPena = withContext(Dispatchers.IO) {
        val pedido = JSONObject()
            .put("anos", dados.anos)
            .put("meses", dados.meses)
            .put("dias", dados.dias)
            .put("regime", dados.regime.name)
            .put("tipoCrime", dados.tipoCrime.name)
            .put("reincidente", dados.reincidente)
            .put("dataFato", dados.dataFato.toString())
            .put("inicio", dados.inicio.toString())
            .put("referencia", dados.referencia.toString())
            .put("casoSimples", dados.casoSimples)

        val conexao = URL(ENDERECO).openConnection() as HttpURLConnection
        try {
            conexao.requestMethod = "POST"
            conexao.connectTimeout = 5000
            conexao.readTimeout = 10000
            conexao.doOutput = true
            conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conexao.setRequestProperty("Accept", "application/json")
            conexao.outputStream.use {
                it.write(pedido.toString().toByteArray(Charsets.UTF_8))
            }

            val status = conexao.responseCode
            val corpo = (if (status in 200..299) conexao.inputStream else conexao.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

            if (status in 400..499) {
                val mensagem = runCatching { JSONObject(corpo).getString("erro") }
                    .getOrDefault("Dados inválidos ou caso não suportado.")
                throw IllegalArgumentException(mensagem)
            }
            if (status !in 200..299) {
                throw IOException("Erro HTTP $status no servidor.")
            }

            val json = try {
                JSONObject(corpo)
            } catch (_: Exception) {
                throw IOException("Resposta JSON inválida do servidor.")
            }
            val regra = json.getJSONObject("regra")
            ResultadoPena(
                regimeSeguinte = Regime.valueOf(json.getString("regimeSeguinte")),
                regra = RegraProgressao(
                    descricao = regra.getString("descricao"),
                    fundamento = regra.getString("fundamento")
                ),
                totalDias = json.getLong("totalDias"),
                requisitoDias = json.getLong("requisitoDias"),
                diasCumpridos = json.getLong("diasCumpridos"),
                diasRestantes = json.getLong("diasRestantes"),
                diasParaProgressao = json.getLong("diasParaProgressao"),
                dataProgressao = LocalDate.parse(json.getString("dataProgressao")),
                terminoTeorico = LocalDate.parse(json.getString("terminoTeorico"))
            )
        } finally {
            conexao.disconnect()
        }
    }
}
