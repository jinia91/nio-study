package com

import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


object EchoWithPoolServer {
    @JvmStatic
    fun main(args: Array<String>) {
        val log = mu.KotlinLogging.logger {}
        val echoServerThreadPool: ExecutorService = Executors.newFixedThreadPool(1000)
        val echoServerPort = 7778

        log.info { "EchoWithPoolServer started" }

        ServerSocket(echoServerPort).use { echoServerSocket ->
            while (true) {
                val clientSocket = echoServerSocket.accept()
                echoServerThreadPool.submit {
                    log.info { "Client connected port : ${clientSocket.port}" }
                    handleEchoWithPoolClient(clientSocket)
                }
            }
        }
    }

    fun handleEchoWithPoolClient(socket: Socket) {
        val input: InputStream = socket.getInputStream()
        val output: OutputStream = socket.getOutputStream()

        while (true) {
            val bytes = ByteArray(1024)
            val bytesRead = input.read(bytes)
//            interval()
            val msg = "echo : ${String(bytes, 0, bytesRead)}"
            output.write(msg.toByteArray(), 0, msg.toByteArray().size)
            output.flush()
        }
    }


    private fun interval() {
        Thread.sleep(500)
    }

}


