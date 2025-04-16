package net.tomczek.ar.puzzle.solver

class ImageProcessingException(message: String, cause: Throwable?) : Exception(message, cause) {
    constructor(message: String) : this(message, null)
    constructor(cause: Throwable) : this(cause.message ?: "Unknown error", cause)
}