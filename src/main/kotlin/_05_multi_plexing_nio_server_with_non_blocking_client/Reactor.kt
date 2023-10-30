package _05_multi_plexing_nio_server_with_non_blocking_client

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel

private val log = mu.KotlinLogging.logger {}

class Reactor (port: Int) : Runnable {
    private val selector: Selector = Selector.open()
    private val serverSocketChannel: ServerSocketChannel = ServerSocketChannel.open()
    private var loopCnt = 0

    init {
        serverSocketChannel.socket().bind(InetSocketAddress(port))
        serverSocketChannel.configureBlocking(false)
        val selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
        selectionKey.attach(AcceptHandler(selector, serverSocketChannel))
    }

    override fun run() {
        try {
            while (true) {
                selector.select()
                loopCnt++
                log.info { "loopCnt: $loopCnt" }
                val selected: MutableSet<SelectionKey> = selector.selectedKeys()
                for (selectionKey in selected) {
                    log.info { "selectionKey: ${selectionKey.attachment()}" }
                    dispatch(selectionKey)
                }
                selected.clear()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun dispatch(selectionKey: SelectionKey) {
        val handler: Handler = selectionKey.attachment() as Handler
        handler.handle()
    }
}

