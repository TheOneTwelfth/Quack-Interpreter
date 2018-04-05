//Simple LinkedList-like queue implementation
class Queue<T> {

    private var first: Node<T>? = null
    private var last: Node<T>? = null

    fun add(data: T) {

        val newNode = Node(data, null)

        if (first == null) {
            first = newNode
            last = newNode
        }
        else {
            last?.next = newNode
            last = newNode
        }

    }

    fun remove(): T? {

        return if (!isEmpty()) {
            val data = first!!.data
            first = first?.next
            data
        }
        else
            null

    }

    fun isEmpty(): Boolean { return first == null }

    class Node<T>(val data: T, var next: Node<T>?)

}