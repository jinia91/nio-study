package _06_http_nio_server

import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

object App {
    @JvmStatic
    fun main(args: Array<String>) {
        BootStrap()
            .group()
            .channel(ServerSocketChannel.open())
            .localAddress(InetSocketAddress(7777))
            .initHandler(AcceptHandler())
            .run()
    }
}