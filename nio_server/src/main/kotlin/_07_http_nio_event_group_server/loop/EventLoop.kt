package _07_http_nio_event_group_server.loop

import _07_http_nio_event_group_server.handler.Context
import _07_http_nio_event_group_server.handler.Handler
import java.io.IOException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

abstract class EventLoop(
    protected val selector: Selector = Selector.open()
) : Runnable {

    override fun run() {
        loop()
    }

    private fun loop() {
        try {
            while (true) {
                selector.select()
                val selected: MutableSet<SelectionKey> = selector.selectedKeys()
                for (selectionKey in selected) {
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
        val context = Context(selectionKey = selectionKey, eventLoop = this)
        handler.handle(context)
    }
}