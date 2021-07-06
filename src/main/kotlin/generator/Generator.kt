package generator

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import dto.EdgeDTO
import dto.NetworkDTO
import dto.NodeDTO
import java.io.File
import java.io.FileReader
import java.lang.reflect.Type
import kotlin.random.Random


class Generator(val n: Int) {
    private val serializer = Gson()

     fun makeNetwork(type: NetworkType, m: Int? = 0): NetworkDTO {
        return when (type) {
            NetworkType.RING -> generateRing()
            NetworkType.RANDOM -> generateRandom(m!!)
            NetworkType.FULL -> generateFull()
            else -> throw IllegalArgumentException()
        }
    }

    private fun generateRandom(m: Int): NetworkDTO {
        val size = n
        val nodes = mutableListOf<NodeDTO>()
        val edges = mutableListOf<EdgeDTO>()
        val nodesDegree =  IntArray(size) { 0 }

        for (i in 0 until size) {
            nodes.add(NodeDTO(i, "node-$i"))
        }

        for (i in 0 until size) {
            var found = false
            while (!found) {
                val target = Random.nextInt(size)
                if (nodesDegree[target] <= m){
                    edges.add(EdgeDTO(i, target))
                    found = true
                }
            }
        }

        for (i in 0 until Random.nextInt(n * n)){
            val target = Random.nextInt(size)
            if (nodesDegree[target] <= m){
                edges.add(EdgeDTO(i, target))
            }
        }

        return NetworkDTO(NetworkType.RANDOM, nodes, edges)    }

    private fun generateRing(): NetworkDTO {
        val size = n
        val nodes = mutableListOf<NodeDTO>()
        val edges = mutableListOf<EdgeDTO>()

        for (i in 0 until size) {
            nodes.add(NodeDTO(i, "node-$i"))
        }
        for (i in 0 until size) {
            edges.add(EdgeDTO(i, (i + 1) % size))
        }
        return NetworkDTO(NetworkType.RING, nodes, edges)
    }

    private fun generateFull(): NetworkDTO {
        val size = n
        val nodes = mutableListOf<NodeDTO>()
        val edges = mutableListOf<EdgeDTO>()

        for (i in 0 until size) {
            nodes.add(NodeDTO(i, "node-$i"))
            for (j in i..size) {
                edges.add(EdgeDTO(i, j))
            }
        }
        return NetworkDTO(NetworkType.FULL, nodes, edges)
    }

    fun serialize(filename: String, network: NetworkDTO) {
        File(filename).writeText(serializer.toJson(network))
    }

    fun deserialize(filename: String): NetworkDTO? {
        val type: Type = object : TypeToken<NetworkDTO>() {}.type
        val gson = Gson()
        val reader = JsonReader(FileReader(filename))
        return gson.fromJson<NetworkDTO>(reader, type)
    }
}