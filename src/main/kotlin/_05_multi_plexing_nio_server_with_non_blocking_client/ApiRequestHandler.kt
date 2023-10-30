package _05_multi_plexing_nio_server_with_non_blocking_client

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

private val log = mu.KotlinLogging.logger {}

private const val ECHO_HOST = "localhost"
private const val ECHO_PORT = 7778

class ApiRequestHandler(
    private val selector: Selector,
    private val context: Context,
    private val parentHandler: BusinessHandler,
) : Handler {
    private var echoSocketChannel: SocketChannel
    private var selectionKey: SelectionKey
    var state: Int

    init {
        echoSocketChannel = SocketChannel.open()
        echoSocketChannel.connect(InetSocketAddress(ECHO_HOST, ECHO_PORT))
        state = CONNECT
        echoSocketChannel.configureBlocking(false)
        selectionKey = echoSocketChannel.register(selector, SelectionKey.OP_CONNECT, this)
    }

    override fun handle() {
        when (state) {
            CONNECT -> {
                log.info { "connect and request Echo" }
                if (echoSocketChannel.finishConnect()) {
                    request()
                    selectionKey.interestOps(SelectionKey.OP_READ)
                    state = READING
                }
            }

            READING -> {
                log.info { "receive Echo" }
                receive()
                cleanup()
                state = FINISH
                parentHandler.selectionKey.interestOps(SelectionKey.OP_WRITE)
            }
        }
    }

    private fun cleanup() {
        try {
            selectionKey.cancel() // Selector에서 키를 취소합니다.
            echoSocketChannel.close() // 채널을 닫습니다.
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun receive() {
        val buffer = ByteBuffer.allocate(256)
        val bytesRead = echoSocketChannel.read(buffer)
        if (bytesRead > 0) {
            buffer.flip()
            context.buffer.put(buffer)
            context.buffer.flip()
        }
    }

    private fun request() {
        echoSocketChannel.write(context.buffer)
        context.buffer.clear()
    }

    companion object {
        const val CONNECT = 0
        const val SENDING = 1
        const val READING = 2
        const val FINISH = 3
    }
}
