package _06_http_nio_server

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

private val log = mu.KotlinLogging.logger {}

object ConnectionPool {
    private val pool: MutableList<SocketChannel> = mutableListOf()
    private val POOL_SIZE = 1000
    private val ECHO_HOST = "localhost"
    private val ECHO_PORT = 7778
    private val DELAY_MS = 50L

    fun init() {
        repeat(POOL_SIZE) {
            try {
                val echoSocketChannel = SocketChannel.open()
                echoSocketChannel.connect(InetSocketAddress(ECHO_HOST, ECHO_PORT))
                echoSocketChannel.configureBlocking(false)
                pool.add(echoSocketChannel)

            } catch (ex: IOException) {
                log.error { "Connection initialization error: ${ex.message}" }  // 로그 찍기
            }
        }
    }

    @Synchronized
    fun borrow(): SocketChannel? {
        return if (pool.isNotEmpty()) pool.removeAt(pool.size - 1) else null
    }

    @Synchronized
    fun release(socketChannel: SocketChannel) {
        pool.add(socketChannel)
    }
}