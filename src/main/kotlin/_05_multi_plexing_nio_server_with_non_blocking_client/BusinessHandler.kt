package _05_multi_plexing_nio_server_with_non_blocking_client

import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

private val log = mu.KotlinLogging.logger {}

class BusinessHandler(
    private val selector: Selector,
    private val socketChannel: SocketChannel
) : Handler {
    val selectionKey: SelectionKey = socketChannel.register(selector, SelectionKey.OP_READ)
    private val context: Context = Context(ByteBuffer.allocate(256))
    private var childHandler: ApiRequestHandler? = null

    init {
        selectionKey.attach(this)
        selector.wakeup()
    }

    override fun handle() {
        if (selectionKey.isReadable) {
            log.info("read client request")
            read()
            childHandler = ApiRequestHandler(selector, context, this)
            childHandler!!.handle()
        }
        else if (selectionKey.isWritable && childHandler?.state == ApiRequestHandler.FINISH) {
            write()
        }
    }

    private fun read() {
        log.info { "business handler start read" }
        socketChannel.read(context.buffer)
        selectionKey.interestOps(0)
        context.buffer.flip()
    }

    private fun write() {
        log.info { "business handler start write" }
        socketChannel.write(context.buffer)
        context.buffer.clear()
        selectionKey.interestOps(SelectionKey.OP_READ)
    }
}
