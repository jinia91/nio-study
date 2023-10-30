package _05_multi_plexing_nio_server_with_non_blocking_client

import java.io.IOException
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel


class AcceptHandler(
    private val selector: Selector,
    private val serverSocketChannel: ServerSocketChannel,
) : Handler {
    override fun handle() {
        try {
            val socketChannel = serverSocketChannel.accept()
            socketChannel.configureBlocking(false)
            socketChannel?.let { BusinessHandler(selector, it) }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}