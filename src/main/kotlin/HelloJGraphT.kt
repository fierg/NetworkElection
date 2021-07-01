import org.jgrapht.Graph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import org.jgrapht.traverse.DepthFirstIterator
import java.io.StringWriter
import java.io.Writer
import java.net.URI
import java.net.URISyntaxException
import java.rmi.server.ExportException

class HelloJGraphT {
    /**
     * A simple introduction to using JGraphT.
     *
     * @author Barak Naveh
     */
    object HelloJGraphT {
        /**
         * The starting point for the demo.
         *
         * @param args ignored.
         *
         * @throws URISyntaxException if invalid URI is constructed.
         * @throws ExportException if graph cannot be exported.
         */
        @Throws(URISyntaxException::class, ExportException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val stringGraph = createStringGraph()

            // note undirected edges are printed as: {<v1>,<v2>}
            println("-- toString output")
            println(stringGraph.toString())
            println()


            // create a graph based on URI objects
            val hrefGraph = createHrefGraph()

            // find the vertex corresponding to www.jgrapht.org
            val start = hrefGraph
                .vertexSet().stream().filter { uri: URI -> uri.host == "www.jgrapht.org" }.findAny()
                .get()


            // perform a graph traversal starting from that vertex
            println("-- traverseHrefGraph output")
            traverseHrefGraph(hrefGraph, start)
            println()
            println("-- renderHrefGraph output")
            renderHrefGraph(hrefGraph)
            println()
        }

        /**
         * Creates a toy directed graph based on URI objects that represents link structure.
         *
         * @return a graph based on URI objects.
         */
        @Throws(URISyntaxException::class)
        private fun createHrefGraph(): Graph<URI, DefaultEdge> {
            val g: Graph<URI, DefaultEdge> = DefaultDirectedGraph(
                DefaultEdge::class.java
            )
            val google = URI("http://www.google.com")
            val wikipedia = URI("http://www.wikipedia.org")
            val jgrapht = URI("http://www.jgrapht.org")

            // add the vertices
            g.addVertex(google)
            g.addVertex(wikipedia)
            g.addVertex(jgrapht)

            // add edges to create linking structure
            g.addEdge(jgrapht, wikipedia)
            g.addEdge(google, jgrapht)
            g.addEdge(google, wikipedia)
            g.addEdge(wikipedia, google)
            return g
        }

        /**
         * Traverse a graph in depth-first order and print the vertices.
         *
         * @param hrefGraph a graph based on URI objects
         *
         * @param start the vertex where the traversal should start
         */
        private fun traverseHrefGraph(hrefGraph: Graph<URI, DefaultEdge>, start: URI) {
            val iterator: Iterator<URI> = DepthFirstIterator(hrefGraph, start)
            while (iterator.hasNext()) {
                val uri = iterator.next()
                println(uri)
            }
        }

        /**
         * Render a graph in DOT format.
         *
         * @param hrefGraph a graph based on URI objects
         */
        @Throws(ExportException::class)
        private fun renderHrefGraph(hrefGraph: Graph<URI, DefaultEdge>) {
            val exporter = DOTExporter<URI, DefaultEdge> { v: URI ->
                v.host.replace('.', '_')
            }
            exporter.setVertexAttributeProvider { v: URI ->
                val map: MutableMap<String, Attribute> =
                    LinkedHashMap()
                map["label"] = DefaultAttribute.createAttribute(v.toString())
                map
            }
            val writer: Writer = StringWriter()
            exporter.exportGraph(hrefGraph, writer)
            println(writer.toString())
        }

        /**
         * Create a toy graph based on String objects.
         *
         * @return a graph based on String objects.
         */
        private fun createStringGraph(): Graph<String, DefaultEdge> {
            val g: Graph<String, DefaultEdge> = SimpleGraph(DefaultEdge::class.java)
            val v1 = "v1"
            val v2 = "v2"
            val v3 = "v3"
            val v4 = "v4"

            // add the vertices
            g.addVertex(v1)
            g.addVertex(v2)
            g.addVertex(v3)
            g.addVertex(v4)

            // add edges to create a circuit
            g.addEdge(v1, v2)
            g.addEdge(v2, v3)
            g.addEdge(v3, v4)
            g.addEdge(v4, v1)
            return g
        }
    }
}