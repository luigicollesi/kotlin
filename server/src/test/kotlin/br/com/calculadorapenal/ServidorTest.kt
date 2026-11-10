package br.com.calculadorapenal

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServidorTest {
    private val jsonValido = """
        {
          "anos": 1,
          "meses": 0,
          "dias": 0,
          "regime": "FECHADO",
          "tipoCrime": "COMUM_SEM_VIOLENCIA",
          "reincidencia": "PRIMARIO",
          "dataFato": "2025-01-01",
          "inicio": "2025-02-01",
          "referencia": "2025-02-01",
          "casoSimples": true
        }
    """.trimIndent()

    @Test
    fun servidorRespondeHealth() = testApplication {
        application { configurarServidor() }
        val resposta = client.get("/health")
        assertEquals(HttpStatusCode.OK, resposta.status)
        assertTrue(resposta.bodyAsText().contains("\"ok\""))
    }

    @Test
    fun calculaPeloEndpoint() = testApplication {
        application { configurarServidor() }
        val resposta = client.post("/api/calcular") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(jsonValido)
        }
        assertEquals(HttpStatusCode.OK, resposta.status)
        val dados = Json.decodeFromString<RespostaCalculo>(resposta.bodyAsText())
        assertEquals(59L, dados.requisitoDias)
        assertEquals("2025-03-31", dados.dataProgressao)
        assertEquals("16%", dados.regra.descricao)
    }

    @Test
    fun rejeitaDadosInvalidos() = testApplication {
        application { configurarServidor() }
        val resposta = client.post("/api/calcular") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(jsonValido.replace("\"anos\": 1", "\"anos\": -1"))
        }
        assertEquals(HttpStatusCode.BadRequest, resposta.status)
        assertTrue(resposta.bodyAsText().contains("erro"))
    }

    @Test
    fun rejeitaCasoNaoSuportado() = testApplication {
        application { configurarServidor() }
        val resposta = client.post("/api/calcular") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(jsonValido.replace("\"casoSimples\": true", "\"casoSimples\": false"))
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, resposta.status)
    }

    @Test
    fun rejeitaJsonIncompleto() = testApplication {
        application { configurarServidor() }
        val resposta = client.post("/api/calcular") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("{}")
        }
        assertEquals(HttpStatusCode.BadRequest, resposta.status)
    }
    @Test
    fun rejeitaReincidenciaDesconhecida() = testApplication {
        application { configurarServidor() }
        val resposta = client.post("/api/calcular") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(jsonValido.replace("\"PRIMARIO\"", "\"NAO_INFORMADO\""))
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, resposta.status)
    }

    @Test
    fun rejeitaRegimeInvalido() = testApplication {
        application { configurarServidor() }
        val resposta = client.post("/api/calcular") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(jsonValido.replace("\"FECHADO\"", "\"DESCONHECIDO\""))
        }
        assertEquals(HttpStatusCode.BadRequest, resposta.status)
    }

    @Test
    fun aplica25PorCentoAoReincidenteGenericoPorCrimeViolentoAnteriorA2026() =
        testApplication {
            application { configurarServidor() }
            val corpo = jsonValido
                .replace("COMUM_SEM_VIOLENCIA", "COMUM_COM_VIOLENCIA")
                .replace("PRIMARIO", "ANTERIOR_SEM_VIOLENCIA")
            val resposta = client.post("/api/calcular") {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(corpo)
            }
            assertEquals(HttpStatusCode.OK, resposta.status)
            val resultado = Json.decodeFromString<RespostaCalculo>(resposta.bodyAsText())
            assertEquals(92L, resultado.requisitoDias)
        }

}
