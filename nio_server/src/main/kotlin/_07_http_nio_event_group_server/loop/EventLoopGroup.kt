package _07_http_nio_event_group_server.loop

import _07_http_nio_event_group_server.handler.AcceptorHandler
import _07_http_nio_event_group_server.handler.Handler
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

class EventLoopGroup(
    port: Int,
    workerCount: Int = getWorkerDefaultCount(),
    private val serverListenSocketChannel: ServerSocketChannel,
) : EventLoop() {
    private val workerLoops : Array<WorkerLoop>

    init {
        workerLoops = Array(workerCount) { WorkerLoop() }
        workerLoops.forEach { worker ->
            Thread(worker).apply {
                isDaemon = true
                start()
            }
        }

        serverListenSocketChannel.socket().bind(InetSocketAddress(port))
        serverListenSocketChannel.configureBlocking(false)
    }

    fun initChain(handler: Handler): Handler {
        serverListenSocketChannel.register(selector, SelectionKey.OP_ACCEPT, AcceptorHandler(serverListenSocketChannel, workerLoops, handler))
        return handler
    }

    companion object{
        private fun getWorkerDefaultCount(): Int {
            val coreCount = Runtime.getRuntime().availableProcessors()
            return maxOf(coreCount - 1, 4)
        }
    }
}

