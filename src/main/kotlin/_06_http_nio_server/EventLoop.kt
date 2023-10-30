package _06_http_nio_server

import java.io.IOException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

private val log = mu.KotlinLogging.logger {}

class EventLoop () : Runnable {
    val selector: Selector = Selector.open()
    private var loopCnt = 0

    override fun run() {
        log.info { "EventLoop started" }
        try {
            while (true) {
                selector.select()
                loopCnt++
                log.info { "loopCnt: $loopCnt" }
                val selected: MutableSet<SelectionKey> = selector.selectedKeys()
                for (selectionKey in selected) {
                    log.debug { "selectionKey: ${selectionKey.attachment()}" }
                    dispatch(selectionKey)
                }
                selected.clear()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun dispatch(selectionKey: SelectionKey) {
        val handler: Handler = selectionKey.attachment() as Handler
        handler.handle()
    }
}

