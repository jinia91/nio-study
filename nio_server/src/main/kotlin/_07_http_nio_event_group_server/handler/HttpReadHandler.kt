package _07_http_nio_event_group_server.handler

import java.nio.ByteBuffer
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

class HttpReadHandler(
    override var nextHandler: Handler,
) : Handler {
    private val contentRegex = """"content"\s*:\s*"([^"]*)"""".toRegex()

    override fun handle(context: Context) {
        val selectionKey = context.selectionKey
        val eventLoop = context.eventLoop
        val socketChannel = selectionKey.channel() as SocketChannel
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
        selectionKey.interestOps(0)
    }
}
