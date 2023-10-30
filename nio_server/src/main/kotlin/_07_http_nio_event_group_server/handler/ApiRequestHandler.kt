package _07_http_nio_event_group_server.handler

import _07_http_nio_event_group_server.ConnectionPool
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode.FINISH
import sun.tools.jconsole.Messages.CONNECT
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.text.AttributedCharacterIterator.Attribute.READING
import kotlin.coroutines.jvm.internal.CompletedContinuation.context

class ApiRequestHandler(
    override var nextHandler: Handler
) : Handler {
    override fun handle(context: Context) {
        if (echoSocketChannel.finishConnect()) {
            request()
            selectionKey.interestOps(SelectionKey.OP_READ)
            state = READING
        }
    }

//            READING -> {
//                receive()
//                cleanup()
//                state = FINISH
//                parentHandler.selectionKey.interestOps(SelectionKey.OP_WRITE)
//            }
}
}

private fun request() {
    echoSocketChannel.write(ByteBuffer.wrap(context.buffer.toByteArray()))
}

private fun receive() {
    val buffer = ByteBuffer.allocate(256)
    val bytesRead = echoSocketChannel.read(buffer)
    if (bytesRead > 0) {
        buffer.flip()
        val response = String(buffer.array(), 0, bytesRead)
        context.buffer = response
    }
}

private fun cleanup() {
    try {
        ConnectionPool.release(echoSocketChannel)
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
}

companion object {
    const val CONNECT = 0
    const val SENDING = 1
    const val READING = 2
    const val FINISH = 3
}
}