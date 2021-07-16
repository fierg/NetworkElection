package visualization

import com.mxgraph.layout.mxCircleLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.util.mxCellRenderer
import generator.Generator
import generator.NetworkType
import network.Network
import network.Node
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultEdge
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO


class VisualDemo {

}
fun main() {
    val nNodes = 10
    val g = Generator(nNodes)
    val network = g.makeNetwork(NetworkType.RANDOM)
    g.serialize("data/random_graph.txt", network)
    g.deserialize("data/random_graph.txt")
    val n = Network(nNodes, "log.txt")
    // Create network
    n.fromNetworkDTO(network)
    val graphAdapter: JGraphXAdapter<Node, DefaultEdge> = JGraphXAdapter(n.graph)
    val layout: mxIGraphLayout = mxCircleLayout(graphAdapter)
    layout.execute(graphAdapter.getDefaultParent())

    val image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2.0, Color.WHITE, true, null)
    val imgFile = File("data/random_graph.png")
    ImageIO.write(image, "PNG", imgFile)

}