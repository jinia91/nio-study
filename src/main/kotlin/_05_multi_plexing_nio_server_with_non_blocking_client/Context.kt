package _05_multi_plexing_nio_server_with_non_blocking_client

import java.nio.ByteBuffer

class Context(
    val buffer: ByteBuffer,
)