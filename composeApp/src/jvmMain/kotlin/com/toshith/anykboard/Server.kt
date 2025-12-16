package com.toshith.anykboard

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.routing
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.*
//import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

const val PORT = 11200

class Server(
    val secret: String,
) {
    private val keyboard = KeyboardExecutor()

    fun start() {
        //val log = LoggerFactory.getLogger("Server")

        embeddedServer(Netty, port = PORT) {
            install(WebSockets) {
                pingPeriod = 15.seconds
                timeout = 30.seconds
            }

            routing {
                webSocket {
                    log.info("Client connected")

                    send(Frame.Text("""{"type":"hello"}"""))

                    val authFrame = incoming.receive() as Frame.Text
                    val auth = Json.parseToJsonElement(authFrame.readText()).jsonObject

                    if (auth["type"]?.jsonPrimitive?.content != "auth") {
                        close()
                        return@webSocket
                    }

                    val decrypted = Crypto.decrypt(
                        auth["data"]!!.jsonPrimitive.content
                    )

                    if (decrypted != secret) {
                        close()
                        return@webSocket
                    }

                    log.info("Authenticated")
                    send(Frame.Text("""{"type":"ok"}"""))

                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val msg = Json.parseToJsonElement(frame.readText()).jsonObject
                            log.info(msg.toString())
                            when (msg["type"]?.jsonPrimitive?.content) {
                                "ping" ->
                                    send(Frame.Text("""{"type":"pong"}"""))

                                "key" -> {
                                    val combo = Crypto.decrypt(
                                        msg["data"]!!.jsonPrimitive.content
                                    )
                                    keyboard.pressCombo(combo)
                                }

                                "bulk-type" -> {
                                    val text = Crypto.decrypt(msg["data"]!!.jsonPrimitive.content)
                                    keyboard.bulkType(text)
                                }

                                "bulk-paste" -> {
                                    val text = Crypto.decrypt(msg["data"]!!.jsonPrimitive.content)
                                    keyboard.bulkPaste(text)
                                }
                            }
                        }
                    }
                }
            }
        }.start(wait = false)
    }
}
