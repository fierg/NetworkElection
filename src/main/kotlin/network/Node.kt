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
            sleep(Random.nextInt(500))
            if (Random.nextDouble(0.0,1.0) < p){
                startElection()
            } else {
                sleep(500 + Random.nextInt(100))
            }
        }
    }

    private fun startElection() {
        System.out.println("Node $id - started election")
        val m: Message = Message().add("ELECTION", id)
        network.sendToNeighbors(id, m.toJson())
    }

    private fun declareWin() {
        System.out.println("Node $id - declared win")
        val m: Message = Message().add("WIN", id)
        network.sendToNeighbors(id, m.toJson())
    }

    private fun propagateElection(leadingID: Int){
        currentElectionLeader = leadingID
        System.out.println("Node $id - propagated election - leading id is $leadingID")
        val m: Message = Message().add("ELECTION", leadingID)
        network.sendToNeighbors(id, m.toJson())
    }

    private fun propagateWin(leadingID: Int){
        currentElectionLeader = Int.MIN_VALUE
        System.out.println("Node $id - propagated win - winner id is $leadingID")
        val m: Message = Message().add("WIN", leadingID)
        network.sendToNeighbors(id, m.toJson())
    }

    internal open fun handleMessage(loops: Int, filepath: String): Boolean {
        network.incrementMessageCounter()
        val rm: NodeMessage = network.receive(id) ?: return true
        val m: Message = Message.fromJson(rm.payload)
        //println("node: $id handling message ${m.toJson()}")

        //val hopcount = handleHopCount(m)
        //handleLogging(loops, hopcount, filepath)

        if (m.query("ELECTION") != null){
        when {
            m.query("ELECTION")!!.toInt() == id -> declareWin()
            m.query("ELECTION")!!.toInt() > currentElectionLeader -> propagateElection(m.query("ELECTION")!!.toInt())
            m.query("ELECTION")!!.toInt() <= currentElectionLeader -> System.out.println("Node $id - ignoring election desire from ${m.query("ELECTION")!!.toInt()} - current lead is $currentElectionLeader")
        }}
        if (m.query("WIN") != null){
            when {
                m.query("WIN")!!.toInt() == id -> {
                    currentElectionLeader = Int.MIN_VALUE
                    System.out.println("########### node $id wins election ###########")
                }
                m.query("WIN")!!.toInt() == currentElectionLeader -> propagateWin(m.query("WIN")!!.toInt())

            }}

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