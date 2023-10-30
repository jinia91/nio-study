package _07_http_nio_event_group_server.handler

import _07_http_nio_event_group_server.loop.EventLoop
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey

class Context(
    var buffer: String = "",
    val selectionKey: SelectionKey,
    val eventLoop: EventLoop
)