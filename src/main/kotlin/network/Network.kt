package network

import dto.NetworkDTO
import generator.NetworkType
import network.message.MessageQueue
import network.message.MessageType
import network.message.NodeMessage
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import java.util.concurrent.atomic.AtomicInteger

class Network(
    private val n_nodes: Int,
    val filepath: String,
    val timeStamp: String? = null
) {
    private val mqueues: Array<MessageQueue?> = arrayOfNulls(n_nodes)
    private val graph: Graph<Node, DefaultEdge> = SimpleGraph(DefaultEdge::class.java)
    var vertices: List<Node>? = null
    var type: NetworkType? = null

    init {
        for (i in 0 until n_nodes) mqueues[i] = MessageQueue()
    }

    companion object {
        val messageCount = AtomicInteger(0)
    }

    fun incrementMessageCounter(): Int {
        return messageCount.incrementAndGet()
    }

    fun getMessageCounter(): Int {
        return messageCount.get()
    }

    fun fromNetworkDTO(networkDTO: NetworkDTO) {
        networkDTO.nodes.forEach {
            graph.addVertex(Node(it.id, n_nodes, this, filepath))
        }
        vertices = graph.vertexSet().sortedBy { it.id }
        networkDTO.edges.forEach {
            graph.addEdge(vertices!![it.from], vertices!![it.to])
        }
        type = networkDTO.type
    }

    fun unicast(sender_id: Int, receiver_id: Int, message: String) {
        if (receiver_id < 0 || receiver_id >= n_nodes) {
            System.err.printf("Network::unicast: unknown receiver id %d\n", receiver_id)
            return
        }
        if (sender_id < 0 || sender_id >= n_nodes) {
            System.err.printf("Network::unicast: unknown sender id %d\n", sender_id)
            return
        }
        val raw = NodeMessage(sender_id, receiver_id, MessageType.UNICAST, message)
        mqueues[receiver_id]?.put(raw)
    }

    fun multicast(sender_id: Int, receiver_ids: List<Int>, message: String) {
        for (receiverId in receiver_ids) {
            if (receiverId < 0 || receiverId >= n_nodes) {
                System.err.printf("Network::multicast: unknown receiver id %d\n", receiver_ids)
                return
            }
            if (receiverId < 0 || receiverId >= n_nodes) {
                System.err.printf("Network::multicast: unknown sender id %d\n", sender_id)
                return
            }
            val raw = NodeMessage(sender_id, receiverId, MessageType.MULTICAST, message)
            mqueues[receiverId]?.put(raw)
        }
    }

    fun broadcast(sender_id: Int, message: String) {
        if (sender_id < -1 || sender_id >= n_nodes) {
            System.err.printf("Network::broadcast: unknown sender id %d\n", sender_id)
            return
        }
        val raw = NodeMessage(sender_id, -1, MessageType.BROADCAST, message)
        for (l in 0 until n_nodes) {
            if (l == sender_id) continue
            raw.receiverId = l
            mqueues[l]?.put(raw)
        }
    }

    fun sendToNeighbors(sender_id: Int, message: String) {
        if (sender_id < -1 || sender_id >= n_nodes) {
            System.err.printf("Network::broadcast: unknown sender id %d\n", sender_id)
            return
        }

        val neighbors =
            graph.edgesOf(vertices!![sender_id]).toMutableSet().filter { graph.getEdgeSource(it).id == sender_id }
                .map { graph.getEdgeTarget(it).id }
        multicast(sender_id, neighbors, message)
    }

    fun receive(receiver_id: Int): NodeMessage? {
        if (receiver_id < 0 || receiver_id >= n_nodes) {
            System.err.printf("Network::receive: unknown receiver id %d\n", receiver_id)
            return null
        }
        return mqueues[receiver_id]?.waitForMessage()
    }

    fun stop() {
        for (mq in mqueues) mq?.stop()
    }
}