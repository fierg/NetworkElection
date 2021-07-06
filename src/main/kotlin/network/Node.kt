package network

import network.event.Event.logToFile
import network.message.Message
import network.message.NodeMessage


/*
 * Eine Node ist eine Komponente eines verteilten Systems
 */
open class Node(
    internal val id: Int,
    private val n_nodes: Int,
    private val network: Network,
    private val filepath: String,
)  {
    private val t_main: Thread
    private var stop = false

    internal fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
        }
    }

    // Sample implementation is a a token ring simulation with node 0 issuing the
    // first token
    private fun run(filepath: String) {
        issueFirstToken()
        var loops = 0
        while (true) {
            loops++
            if (stop) break
            if (handleMessage(loops, filepath)) break
        }
        println("node: $id stopped.")
    }

    internal open fun handleMessage(loops: Int, filepath: String): Boolean {
        network.incrementMessageCounter()
        val rm: NodeMessage = network.receive(id) ?: return true
        val m: Message = Message.fromJson(rm.payload)
        //println("node: $id handling message ${m.toJson()}")

        val hopcount = handleHopCount(m)
        handleLogging(loops, hopcount, filepath)

        network.unicast(id, (id + 1) % n_nodes, m.toJson())
        println("Resend regular token from $id to ${(id + 1) % n_nodes}")
        return false
    }


    private fun handleHopCount(m: Message): Int {
        var hopcount: Int = m.query("hopcount")!!.toInt()
        hopcount++
        m.add("hopcount", hopcount)
        return hopcount
    }


    private fun handleLogging(loops: Int, hopcount: Int, filepath: String) {
        if (id == 0 && loops % 1000 == 0) {
            logToFile(String.format("hopcount is %d\n", hopcount), filepath)
        }
    }

    internal open fun issueFirstToken() {
        if (id == 0) {
            // I am node 1 and will create the token
            val m: Message = Message().add("token", "true").add("hopcount", 0)
            network.unicast(id, (id + 1) % n_nodes, m.toJson())
        }
    }

    fun stop() {
        stop = true
        try {
            t_main.join()
        } catch (e: InterruptedException) {
            println("Stopping node error:  ${e.message}")
        }
    }

    init {
        t_main = Thread { run(filepath) }
        t_main.start()
    }
}