package simulation

import network.Network
import network.Node
import network.Utils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class Simulator(val m: Int, val nNodes: Int = 10) {
    fun simulate() {

        val timeStamp = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Date())
        val nNodes = 10
        val duration = 4
        val filepath = "log-$timeStamp.txt"
        Utils.log(
            String.format("Simulate %d nodes for %d seconds\n", nNodes.toDouble().pow(2).toInt(), duration),
            filepath
        )

        // Create network
        val network = Network(nNodes, filepath = filepath)

        // Create all nodes and start them
        val nodes = hashMapOf<Int, Node>()
        for (n_id in 0 until nNodes) nodes[n_id] = Node(n_id, nNodes, network, filepath)


        // Wait for the required duration
        Thread.sleep((duration * 1000).toLong())

        Utils.log("Stopping network...", filepath)

        // Stop network - release nodes waiting in receive ...
        network.stop()

        Utils.log("Total messages sent: ${network.getMessageCounter()}")

        // Tell all nodes to stop and wait for the threads to terminate
        for (node in nodes.values) node.stop()

        Utils.log("All nodes stopped. Terminating.", filepath)
    }
}

fun main() {
    val m = Simulator(6)
    m.simulate()
}