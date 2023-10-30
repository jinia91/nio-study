package _07_http_nio_event_group_server.loop

import _07_http_nio_event_group_server.handler.Handler
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

class WorkerLoop : Runnable, EventLoop() {
    fun allocate(socketChannel: SocketChannel, firstHandler: Handler) {
        socketChannel.register(selector, SelectionKey.OP_READ, firstHandler)
    }
}