package _03_multi_plexing_nio_server_with_blocking_client

import _06_http_nio_server.AcceptHandler
import _06_http_nio_server.Handler
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel


class Reactor (port: Int) : Runnable {
    private val selector: Selector = Selector.open()
    private val serverSocketChannel: ServerSocketChannel = ServerSocketChannel.open()

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
                val selected: MutableSet<SelectionKey> = selector.selectedKeys()
                for (selectionKey in selected) {
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

