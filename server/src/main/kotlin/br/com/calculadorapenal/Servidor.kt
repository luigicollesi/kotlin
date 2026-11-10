package br.com.calculadorapenal

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.CancellationException
import java.time.DateTimeException

fun main() {
    val porta = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, host = "127.0.0.1", port = porta) {
        configurarServidor()
    }.start(wait = true)
}

fun Application.configurarServidor() {
    install(ContentNegotiation) { json() }

    routing {
        get("/health") {
            call.respond(RespostaSaude())
        }

        post("/api/calcular") {
            val pedido = try {
                call.receive<PedidoCalculo>()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                call.respond(HttpStatusCode.BadRequest, RespostaErro("JSON inválido ou campos obrigatórios ausentes."))
                return@post
            }

            try {
                val resultado = CalculadoraPena.calcular(pedido.paraDados())
                call.respond(resultado.paraResposta())
            } catch (e: CasoNaoSuportadoException) {
                call.respond(HttpStatusCode.UnprocessableEntity, RespostaErro(e.message ?: "Caso não suportado."))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, RespostaErro(e.message ?: "Dados inválidos."))
            } catch (_: DateTimeException) {
                call.respond(HttpStatusCode.BadRequest, RespostaErro("Informe datas válidas no formato AAAA-MM-DD."))
            }
        }
    }
}
