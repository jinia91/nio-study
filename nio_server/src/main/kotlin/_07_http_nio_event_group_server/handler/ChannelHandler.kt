package _07_http_nio_event_group_server.handler

import _07_http_nio_event_group_server.ConnectionPool
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

private val contentRegex = """"content"\s*:\s*"([^"]*)"""".toRegex()

class ChannelHandler(
    private val selector: Selector,
    private val socketChannel: SocketChannel,
    private val context: Context = Context(),
    private var childHandler: ApiRequestHandler? = null,
) : Handler {
    val selectionKey: SelectionKey = socketChannel.register(selector, SelectionKey.OP_READ, this)

    init {
        selector.wakeup()
    }

    override fun handle() {
        when {
            (selectionKey?.isReadable == true) -> {
                val socketConnection = ConnectionPool.borrow()
                if (socketConnection == null) {
                } else {
                    read()
                    childHandler = ApiRequestHandler(selector, context, this, socketConnection)
                    childHandler?.handle() ?: ApiRequestHandler(selector, context, this, socketConnection).handle()
                }
            }
            (selectionKey?.isWritable == true)-> {
                write()
                gracefulShutdown()
            }
        }
    }

    private fun read() {
        val buff = ByteBuffer.allocate(256)
        socketChannel.read(buff)
        buff.flip()
        val request = StandardCharsets.UTF_8.decode(buff).toString()
        val matchResult = contentRegex.find(request)
        val content = matchResult?.let {
            val content = it.groupValues[1]
            content
        }
        context.buffer = content ?: throw IllegalStateException()
        selectionKey.interestOps(SelectionKey.OP_WRITE)
    }

    private fun write() {

        val responseBody = """
    {
        "content": "${context.buffer}"
    }
"""

        val responseHeaders = """
    HTTP/1.1 200 OK\r
    Content-Type: application/json\r
    Content-Length: ${responseBody.toByteArray(StandardCharsets.UTF_8).size}\r
    Connection: close\r
    \r\n
"""

        val response = responseHeaders + responseBody

        val buffer = ByteBuffer.wrap(response.toByteArray(StandardCharsets.UTF_8))
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer)
        }
    }

    private fun gracefulShutdown() {
        socketChannel.shutdownInput()
        socketChannel.shutdownOutput()
        socketChannel.close()
    }

}