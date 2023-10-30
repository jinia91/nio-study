package _06_http_nio_server

import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

class BootStrap {
    private lateinit var listenChannel : ServerSocketChannel
    private lateinit var inetSocketAddress: InetSocketAddress
    private lateinit var bossGroup: EventLoop
    private lateinit var workerGroup: Array<EventLoop>
    private lateinit var initHandler: AcceptHandler

    fun group(workerCnt: Int = defaultCnt()): BootStrap {
        val workers = Array(workerCnt) { EventLoop() }
        workers.forEach { worker ->
            Thread(worker).apply {
                isDaemon = true
                start()
            }
        }

        this.workerGroup = workers
        return this
    }

    private fun defaultCnt(): Int {
        val coreCount = Runtime.getRuntime().availableProcessors()
        return if (coreCount > 1) coreCount - 1 else 1
    }

    fun channel(serverSocketChannel: ServerSocketChannel): BootStrap {
        this.listenChannel = serverSocketChannel
        return this
    }

    fun localAddress(inetSocketAddress: InetSocketAddress): BootStrap {
        this.inetSocketAddress = inetSocketAddress
        return this
    }

    fun initHandler(handler: AcceptHandler): BootStrap {
        this.initHandler = handler
        return this
    }

    fun run(): BootStrap {
        this.bossGroup = EventLoop()
        ConnectionPool.init()
        this.listenChannel.socket().bind(this.inetSocketAddress)
        this.listenChannel.configureBlocking(false)
        val selectionKey = this.listenChannel.register(this.bossGroup.selector, SelectionKey.OP_ACCEPT, initHandler)
        selectionKey.attach(initHandler)
        initHandler.serverSocketChannel = this.listenChannel
        initHandler.subReactors = this.workerGroup
        bossGroup.run()
        return this
    }
}