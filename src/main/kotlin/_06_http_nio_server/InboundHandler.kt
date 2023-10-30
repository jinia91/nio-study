package _06_http_nio_server

import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

private val log = mu.KotlinLogging.logger {}

class InboundHandler(
    private val selector: Selector,
    private val socketChannel: SocketChannel,
) : Handler {
    val selectionKey: SelectionKey = socketChannel.register(selector, SelectionKey.OP_READ)
    private val context: Context = Context()
    private var childHandler: OutBoundHandler? = null
    private val contentRegex = """"content"\s*:\s*"([^"]*)"""".toRegex()

    init {
        selectionKey.attach(this)
        selector.wakeup()
    }

    override fun handle() {
        if (selectionKey.isReadable) {
            log.debug("read client request")
            read()
            childHandler = OutBoundHandler(selector, context, this)
            childHandler!!.handle()
        } else if (selectionKey.isWritable && childHandler?.state == OutBoundHandler.FINISH) {
            write()
        }
    }

    private fun read() {
        log.debug { "business handler start read" }
        val buff = ByteBuffer.allocate(256)
        socketChannel.read(buff)
        buff.flip()
        val request = StandardCharsets.UTF_8.decode(buff).toString()
        val matchResult = contentRegex.find(request)
        val content = matchResult?.let {
            val content = it.groupValues[1]
            log.info("Content: $content")
            content
        }
        log.debug { content }
        context.buffer = content!!
        selectionKey.interestOps(0)
    }

    private fun write() {
        log.debug { "business handler start write" }

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

        log.debug { response }
        val buffer = ByteBuffer.wrap(response.toByteArray(StandardCharsets.UTF_8))
        socketChannel.write(buffer)
        socketChannel.close()
    }
}
