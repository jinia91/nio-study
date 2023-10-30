package _06_http_nio_server

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

private val log = mu.KotlinLogging.logger {}

class OutBoundHandler(
    private val selector: Selector,
    private val context: Context,
    private val parentHandler: InboundHandler,
) : Handler {
    private var echoSocketChannel: SocketChannel
    private var selectionKey: SelectionKey
    var state: Int

    init {
        echoSocketChannel = ConnectionPool.borrow() ?: throw IllegalStateException("ConnectionPool is empty")
        state = CONNECT
        selectionKey = echoSocketChannel.register(selector, SelectionKey.OP_CONNECT)
        selectionKey.attach(this)
    }

    override fun handle() {
        when (state) {
            CONNECT -> {
                if (echoSocketChannel.finishConnect()) {
                    log.debug { "connect and request Echo" }
                    request()
                    selectionKey.interestOps(SelectionKey.OP_READ)
                    state = READING
                }
            }

            READING -> {
                log.debug { "receive Echo" }
                receive()
                cleanup()
                state = FINISH
                parentHandler.selectionKey.interestOps(SelectionKey.OP_WRITE)
            }
        }
    }

    private fun receive() {
        val buffer = ByteBuffer.allocate(256)
        val bytesRead = echoSocketChannel.read(buffer)
        if (bytesRead > 0) {
            buffer.flip()
            val response = String(buffer.array(), 0, bytesRead)
            context.buffer = response
        }
    }

    private fun cleanup() {
        try {
            ConnectionPool.release(echoSocketChannel)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun request() {
        echoSocketChannel.write(ByteBuffer.wrap(context.buffer.toByteArray()))
    }

    companion object {
        const val CONNECT = 0
        const val SENDING = 1
        const val READING = 2
        const val FINISH = 3
    }
}
