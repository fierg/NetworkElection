package network

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.event.Event.logToFile
import network.message.Message
import network.message.NodeMessage
import kotlin.random.Random


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
    private val p = 0.3
    private var currentElectionLeader = Int.MIN_VALUE

    internal fun sleep(millis: Int) {
        try {
            Thread.sleep(millis.toLong())
        } catch (e: InterruptedException) {
        }
    }

    // Sample implementation is a a token ring simulation with node 0 issuing the
    // first token
    private fun run(filepath: String) {
        startElectionAtRandomTime()
        //issueFirstToken()
        var loops = 0
        while (true) {
            loops++
            if (stop) break
            if (handleMessage(loops, filepath)) break
        }
        println("node: $id stopped.")
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startElectionAtRandomTime(){
        GlobalScope.launch {
            sleep(Random.nextInt(1000))
            if (Random.nextDouble(0.0,1.0) < p){
                startElection()
            } else {
                sleep(1000 + Random.nextInt(1000))
            }
        }
    }

    private fun startElection() {
        System.out.println("Node $id - started election")
        val m: Message = Message().add("ELECTION", id)
        network.sendToNeighbors(id, m.toJson())
    }

    private fun propagateElection(leadingID: Int){
        System.out.println("Node $id - propagated election - leading id is $leadingID")
        val m: Message = Message().add("ELECTION", leadingID)
        network.sendToNeighbors(leadingID, m.toJson())
    }

    internal open fun handleMessage(loops: Int, filepath: String): Boolean {
        network.incrementMessageCounter()
        val rm: NodeMessage = network.receive(id) ?: return true
        val m: Message = Message.fromJson(rm.payload)
        println("node: $id handling message ${m.toJson()}")

        //val hopcount = handleHopCount(m)
        //handleLogging(loops, hopcount, filepath)

        when {
            m.query("ELECTION")?.toInt() ?: Int.MIN_VALUE > currentElectionLeader -> propagateElection(m.query("ELECTION")!!.toInt())

        }

        //network.unicast(id, (id + 1) % n_nodes, m.toJson())
        //println("Resend regular token from $id to ${(id + 1) % n_nodes}")

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