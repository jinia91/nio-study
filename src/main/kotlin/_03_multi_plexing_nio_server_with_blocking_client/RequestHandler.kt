package _03_multi_plexing_nio_server_with_blocking_client

import _02_multi_thread_oio_server.BUF_SIZE
import _02_multi_thread_oio_server.ECHO_SERVER_HOST
import _02_multi_thread_oio_server.ECHO_SERVER_PORT
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

private val log = mu.KotlinLogging.logger {}


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