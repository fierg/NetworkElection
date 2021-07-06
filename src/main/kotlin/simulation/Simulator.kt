package simulation

import generator.Generator
import generator.NetworkType
import network.Network
import network.Node
import network.Utils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class Simulator(val m: Int, val nNodes: Int = 10) {
    fun simulate() {

        val g = Generator(10)
        val network = g.makeNetwork(NetworkType.RING)
        g.serialize("data/graph.txt", network)
        g.deserialize("data/graph.txt")
        val n = Network(10, "log.txt")

        val timeStamp = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Date())
        val nNodes = 10
        val duration = 4
        val filepath = "log-$timeStamp.txt"
        Utils.log(
            String.format("Simulate %d nodes for %d seconds\n", nNodes, duration),
            filepath
        )

        // Create network
        n.fromNetworkDTO(network)

        // Wait for the required duration
        Thread.sleep((duration * 1000).toLong())

        Utils.log("Stopping network...", filepath)

        // Stop network - release nodes waiting in receive ...
        n.stop()

        Utils.log("Total messages sent: ${n.getMessageCounter()}")

        // Tell all nodes to stop and wait for the threads to terminate
        for (node in n.vertices!!) node.stop()

        Utils.log("All nodes stopped. Terminating.", filepath)
    }
}

fun main() {
    val m = Simulator(6)
    m.simulate()
}