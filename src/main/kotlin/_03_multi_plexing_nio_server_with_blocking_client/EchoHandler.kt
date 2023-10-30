package _03_multi_plexing_nio_server_with_blocking_client

import _02_multi_thread_oio_server.BUF_SIZE
import _02_multi_thread_oio_server.ECHO_SERVER_HOST
import _02_multi_thread_oio_server.ECHO_SERVER_PORT
import _06_http_nio_server.Handler
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

private val log = mu.KotlinLogging.logger {}

class EchoHandler(
    selector: Selector,
    private val socketChannel: SocketChannel,
) : Handler {
    private val selectionKey: SelectionKey
    private val buffer: ByteBuffer = ByteBuffer.allocate(256)
    private var state = READING

    init {
        socketChannel.configureBlocking(false)
        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ)
        selectionKey.attach(this)
        selector.wakeup()
    }

    override fun handle() {
        try {
            if (state == READING) {
                read()
                request()
            } else if (state == SENDING) {
                send()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun read() {
        socketChannel.read(buffer)
        selectionKey.interestOps(SelectionKey.OP_WRITE)
        buffer.flip()
        state = SENDING
    }

    private fun request() = Socket(ECHO_SERVER_HOST, ECHO_SERVER_PORT).use { echoSocket ->
        log.info { "prepare request to echo server" }
        val echoOutput: OutputStream = echoSocket.getOutputStream()
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        echoOutput.write(bytes)
        echoOutput.flush()
        val echoInput: InputStream = echoSocket.getInputStream()
        val echoBytes = ByteArray(BUF_SIZE)
        val echoByteRead = echoInput.read(echoBytes)

       val echoMsg = String(echoBytes, 0, echoByteRead)
            .also { log.info { "receive msg : $it" } }
        buffer.clear()
        buffer.put(echoMsg.toByteArray())
        buffer.flip()
    }


    private fun send() {
        socketChannel.write(buffer)
        buffer.clear()
        selectionKey.interestOps(SelectionKey.OP_READ)
        state = READING
    }

    companion object {
        const val READING = 0
        const val SENDING = 1
    }
}
