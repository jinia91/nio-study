package _04_multi_plexing_nio_server_with_blocking_multi_thread_client

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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

private val log = mu.KotlinLogging.logger {}

private val THREAD_POOL = Executors.newFixedThreadPool(5)

class EchoHandler(
    selector: Selector,
    private val socketChannel: SocketChannel,
) : Handler {
    private val selectionKey: SelectionKey
    private val buffer: ByteBuffer = ByteBuffer.allocate(256)
    private var state = READING
    private var future = CompletableFuture<String>()

    init {
        socketChannel.configureBlocking(false)
        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ)
        selectionKey.attach(this)
        selector.wakeup()
    }

    override fun handle() {
        try {
            if (state == READING) {
                log.info { "read" }
                read()
                future = request()
            } else if (state == SENDING && future.isDone) {
                val echoMsg = future.get().also { log.info { "receive msg : $it" } }
                send(echoMsg)
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

    private fun request(): CompletableFuture<String> {
        return CompletableFuture.supplyAsync( {
            Socket(ECHO_SERVER_HOST, ECHO_SERVER_PORT).use { echoSocket ->
                log.info { "prepare request to echo server" }

                val echoOutput: OutputStream = echoSocket.getOutputStream()
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                echoOutput.write(bytes)
                echoOutput.flush()

                val echoInput: InputStream = echoSocket.getInputStream()
                val echoBytes = ByteArray(BUF_SIZE)
                val echoByteRead = echoInput.read(echoBytes)

                String(echoBytes, 0, echoByteRead).also { log.info { "receive msg : $it" } }
            }
        }, THREAD_POOL)
    }


    private fun send(echoMsg: String) {
        socketChannel.write(ByteBuffer.wrap(echoMsg.toByteArray()))
        buffer.clear()
        selectionKey.interestOps(SelectionKey.OP_READ)
        state = READING
    }

    companion object {
        const val READING = 0
        const val SENDING = 1
    }
}
