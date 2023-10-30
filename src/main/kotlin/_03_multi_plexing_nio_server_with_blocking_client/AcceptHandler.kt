package _03_multi_plexing_nio_server_with_blocking_client

import _06_http_nio_server.Handler
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
            socketChannel?.let { EchoHandler(selector, it) }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}