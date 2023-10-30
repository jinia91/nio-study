package _06_http_nio_server

import java.io.IOException
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.atomic.AtomicInteger


class AcceptHandler(
) : Handler {
    lateinit var selector: Selector
    lateinit var serverSocketChannel: ServerSocketChannel
    lateinit var subReactors: Array<EventLoop>
    private var cursor = AtomicInteger(0)

    override fun handle() {
        try {
            val socketChannel = serverSocketChannel.accept()
            socketChannel.configureBlocking(false)
            val worker = subReactors[cursor.get()]
            cursor = getByRoundRobin()
            socketChannel?.let { InboundHandler(worker.selector, it) }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun getByRoundRobin(): AtomicInteger {
        return if (cursor.incrementAndGet() >= subReactors.size) {
            cursor.set(0)
            cursor
        } else {
            cursor
        }
    }
}