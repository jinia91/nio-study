package _07_http_nio_event_group_server

import _07_http_nio_event_group_server.handler.HttpReadHandler
import _07_http_nio_event_group_server.loop.EventLoopGroup
import java.nio.channels.ServerSocketChannel

private val log = mu.KotlinLogging.logger {}

object App {
    @JvmStatic
    fun main(args: Array<String>) {
        doBootstrap()
            .initChain(HttpReadHandler())
            .addChain(HttpReadHandler)
            .addChain(BusinessHandler)
            .addChain(EchoClient)
            .addChain(HttpWriteHandler)
            .start()
    }

    private fun doBootstrap() : EventLoopGroup {
        ConnectionPool.init()
        return EventLoopGroup(
            port = 7777,
            serverListenSocketChannel = ServerSocketChannel.open()
        )
    }
}
