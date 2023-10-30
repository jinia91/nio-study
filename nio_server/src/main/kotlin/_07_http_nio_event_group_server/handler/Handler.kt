package _07_http_nio_event_group_server.handler

interface Handler {
    var nextHandler: Handler
    fun handle(context: Context)
    fun addChain(handler: Handler): Handler {
        nextHandler = handler
        return this
    }
}