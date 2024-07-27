package client.adapters

interface Uint8ArrayAdapter {
    operator fun get(index: Int): UByte
    operator fun set(index: Int, value: UByte)
    val uint8Array: Any
}
