package _02_multi_thread_oio_server

import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val BUF_SIZE = 1024
const val ECHO_SERVER_HOST = "localhost"
const val ECHO_SERVER_PORT = 7778

private val log = mu.KotlinLogging.logger {}

fun main() {
    val port = 7777
    val threadPool: ExecutorService = Executors.newFixedThreadPool(100)

    ServerSocket(port).use { listenSocket ->
        while (true) {
            val connectionSocket = listenSocket.accept()
            threadPool.submit {
                log.info { "Client connected port : ${connectionSocket.port}" }
                handleClient(connectionSocket)
            }
        }
    }
}

private fun handleClient(socket: Socket) = socket.use {
    val input: InputStream = it.getInputStream()
    val output: OutputStream = it.getOutputStream()
    val bytes = ByteArray(BUF_SIZE)
    var byteRead: Int

    while (input.read(bytes).also { byteRead = it } != -1) {
        val request = String(bytes, 0, byteRead)
            .also { log.info { "client request msg : $it" } }
        val response = request(request)
        output.write(response.toByteArray(), 0, response.length)
        output.flush()
    }
    log.info { "Client disconnected port : ${socket.port}" }
}

private fun request(request: String): String {
    var echoMsg = ""
    Socket(ECHO_SERVER_HOST, ECHO_SERVER_PORT).use { echoSocket ->
        log.info { "prepare request to echo server" }
        val echoOutput: OutputStream = echoSocket.getOutputStream()
        echoOutput.write(request.toByteArray(), 0, request.length)
        echoOutput.flush()

        val echoInput: InputStream = echoSocket.getInputStream()
        val echoBytes = ByteArray(BUF_SIZE)
        val echoByteRead = echoInput.read(echoBytes)

        echoMsg = String(echoBytes, 0, echoByteRead)
            .also { log.info { "receive msg : $it" } }
    }
    return echoMsg
}