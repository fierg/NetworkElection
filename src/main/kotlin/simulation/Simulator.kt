package simulation

import generator.Generator
import generator.NetworkType
import network.Network
import network.Node
import network.Utils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class Simulator(val m: Int) {

    /*
    Wie startet der Algorithmus?
    - Random mit einer election

    Wann ist der Algorithmus zu Ende?
    - ring -> wenn ring ein mal umrundet wurde
    - random ->

    Was ist die „Message Extinction“?
    - wenn eine election message nicht weitergeleitet wird auf grund einer bereits besseren konkurenten
     */

    fun simulate() {
        val nNodes = 100

        val g = Generator(nNodes)
        val network = g.makeNetwork(NetworkType.RING)
        g.serialize("data/graph.txt", network)
        g.deserialize("data/graph.txt")
        val n = Network(nNodes, "log.txt")

        val timeStamp = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Date())
        val duration = 10
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