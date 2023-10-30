package _07_http_nio_event_group_server.handler

import _07_http_nio_event_group_server.loop.WorkerLoop
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.atomic.AtomicInteger


class AcceptorHandler(
    private val serverSocketChannel: ServerSocketChannel,
    private val workers: Array<WorkerLoop>,
    override var nextHandler: Handler
) : Handler {
    private var cursor = AtomicInteger(0)

    override fun handle(context: Context) {
        try {
            val socketChannel: SocketChannel = serverSocketChannel.accept()
            socketChannel.configureBlocking(false)
            val worker = workers[cursor.get()]
            cursor = getByRoundRobin()
            worker.allocate(socketChannel, nextHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getByRoundRobin(): AtomicInteger {
        return if (cursor.incrementAndGet() >= workers.size) {
            cursor.set(0)
            cursor
        } else {
            cursor
        }
    }
}