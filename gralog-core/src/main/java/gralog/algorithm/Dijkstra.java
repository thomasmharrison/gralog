package gralog.algorithm;

import gralog.progresshandler.ProgressHandler;
import gralog.structure.Edge;
import gralog.structure.Structure;
import gralog.structure.Vertex;
import gralog.rendering.GralogColor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;



//Djikstra's algorithm description matching gralog style - adds it to list of available algorithms
@AlgorithmDescription(
    name = "Dijkstra",
    text = "Dijkstras algorithm for shortest path",
    url = "https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm"
)


//Dijkstra class
public class Dijkstra extends Algorithm{
    
    public static void dijkstra(Structure<Vertex, Edge> graph, Vertex start, HashMap<Vertex, Vertex> predecessor, HashMap<Vertex, Edge> edgeFromPredecessor, HashMap<Vertex, Double> distance, ProgressHandler progress){
        //priority queue to store vertices ordered by distance from start vertex
        PriorityQueue<Vertex> pQueue = new PriorityQueue<>(Comparator.comparingDouble(distance::get));
        Boolean directed = false;

        //set start vertex as zero distance and all other to infinity
        distance.put(start, 0.0);
        for(Vertex v : graph.getVertices()){
            if (!v.equals(start)) {
                distance.put(v, Double.POSITIVE_INFINITY);
            }
        }

        //add start to priority queue
        pQueue.add(start);

        //continue through vertices in priority queue
        while(!pQueue.isEmpty()){
            Vertex u = pQueue.poll();

            //get all edges incident to u
            for (Edge edge : u.getIncidentEdges()){

                //check if edge is dirceted
                if (edge.isDirected){
                    directed = true;
                }
                //get the other vertex of the edge. if undirected the target may be the same so get the source vertex instead
                Vertex v = edge.getTarget();
                if (directed == false && v.equals(u)){
                    v = edge.getSource();
                }

                //calculate new distance
                double dist = distance.get(u) + edge.weight;

                //if new distance is less than current distance, update distance and add to priority queue
                if (dist < distance.get(v)){
                    distance.put(v, dist);
                    predecessor.put(v, u);
                    edgeFromPredecessor.put(v, edge);
                    pQueue.add(v);
                }
            }
        }
    }

    //loop back from last vertex, colour and change size of edges
    public static void updateShortestPath(Vertex target, HashMap<Vertex, Vertex> predecessor, HashMap<Vertex, Edge> edgeFromPredecessor) {
        Vertex current = target;
        //looping back
        while (predecessor.containsKey(current)) {
            Edge edge = edgeFromPredecessor.get(current);
            edge.color = GralogColor.RED;
            edge.thickness = 0.1;
            current = predecessor.get(current);
        }
    }

    //run method
    public Object run(Structure s, AlgorithmParameters p, Set<Object> selection, ProgressHandler onprogress) {
        HashMap<Vertex, Vertex> predecessor = new HashMap<>();
        HashMap<Vertex, Edge> edgeFromPredecessor = new HashMap<>();
        HashMap<Vertex, Double> distance = new HashMap<>();        
        Vertex v = selectedUniqueVertex(selection);

        if (v == null)
            return "Select one vertex to start Dijkstra's from.";

        //run algorithm
        dijkstra(s, v, predecessor, edgeFromPredecessor, distance, onprogress);

        //find final reachable vertex
        Vertex last = null;
        double maxDistance = 0.0;
        Iterator<Vertex> vertexIterator = s.getVertices().iterator();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();
            if (distance.get(vertex) != Double.POSITIVE_INFINITY && distance.get(vertex) > maxDistance) {
                last = vertex;
                maxDistance = distance.get(vertex);
            }
        }

        updateShortestPath(last, predecessor, edgeFromPredecessor);

        //results
        StringBuilder result = new StringBuilder();
        result.append("Vertex\tDistance\n");
        for (Vertex vertex : distance.keySet()) {
            result.append(vertex.label).append("\t").append(distance.get(vertex)).append("\n");
        }
        //add last vertex
        result.append(last.label).append("\t").append(distance.get(last)).append("\n");
        return result.toString();
    }
}