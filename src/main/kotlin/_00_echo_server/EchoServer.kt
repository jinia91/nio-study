package _00_echo_server

import reactor.core.publisher.Flux.interval
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


private val log = mu.KotlinLogging.logger {}

fun main() {
    val echoServerThreadPool: ExecutorService = Executors.newFixedThreadPool(1000)
    val echoServerPort = 7778

    ServerSocket(echoServerPort).use { echoServerSocket ->
        while (true) {
            val clientSocket = echoServerSocket.accept()
            echoServerThreadPool.submit {
                log.info { "Client connected port : ${clientSocket.port}" }
                handleEchoClient(clientSocket)
            }
        }
    }
}

fun handleEchoClient(socket: Socket) {
    val input: InputStream = socket.getInputStream()
    val output: OutputStream = socket.getOutputStream()

    while (true) {
        val bytes = ByteArray(1024)
        val bytesRead = input.read(bytes)
        val msg = "echo : ${String(bytes, 0, bytesRead)}"
        output.write(msg.toByteArray(), 0, msg.toByteArray().size)
        output.flush()
    }
}

private fun interval() {
    Thread.sleep(60000)
}
