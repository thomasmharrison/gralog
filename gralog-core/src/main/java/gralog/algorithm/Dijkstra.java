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
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;


//Djikstra's algorithm description matching gralog style - adds it to list of available algorithms
@AlgorithmDescription(
    name = "Dijkstra",
    text = "Dijkstras algorithm for shortest path",
    url = "https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm"
)


//Dijkstra class
public class Dijkstra extends Algorithm{

    private static final Logger LOGGER = Logger.getLogger(Dijkstra.class.getName());
    private CountDownLatch latch;
    private static int wait = 1;
    
    public static void dijkstra(Structure<Vertex, Edge> graph, Vertex start, HashMap<Vertex, Vertex> predecessor, HashMap<Vertex, Edge> edgeFromPredecessor, HashMap<Vertex, Double> distance, ProgressHandler progress, CountDownLatch latch) {
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

            //colour the current node
            u.fillColor = GralogColor.GREEN;
            try{
                    progress.onProgress(graph);
                } catch (Exception e){
                    LOGGER.log(Level.SEVERE, "Error with progress", e);
                }

            //get all edges incident to u
            for (Edge edge : u.getIncidentEdges()){

                //colour the current edge
                /*SwingUtilities.invokeLater(() -> {
                    edge.color = GralogColor.GREEN;
                    edge.thickness = 0.1;
                    //graph.repaint();
                });*/
                //progress.update(graph);

                if (edge.getSource().equals(u)) {
                    wait = 1;
                    edge.color = GralogColor.GREEN;
                    edge.thickness = 0.1;
                    //try progress
                    try{
                        progress.onProgress(graph);
                    } catch (Exception e){
                        LOGGER.log(Level.SEVERE, "Error with progress", e);
                    }
                }
                else{
                    wait = 0;
                }
                //edge.color = GralogColor.GREEN;
                //edge.thickness = 0.1;
                //try progress
                /*try{
                    progress.onProgress(graph);
                } catch (Exception e){
                    LOGGER.log(Level.SEVERE, "Error with progress", e);
                }*/

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

                //sleep for x seconds between steps
                try {
                    //Thread.sleep(1000);
                    TimeUnit.SECONDS.sleep(wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //reset edge colours
                edge.color = GralogColor.BLACK;
                edge.thickness = 0.025;
                try{
                    progress.onProgress(graph);
                } catch (Exception e){
                    LOGGER.log(Level.SEVERE, "Error with progress", e);
                }
            }
        
            //reset vertex colour
            u.fillColor = GralogColor.BLACK;
            try{
                progress.onProgress(graph);
            } catch (Exception e){
                LOGGER.log(Level.SEVERE, "Error with progress", e);
            }
        }

    latch.countDown();
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
        this.latch = new CountDownLatch(1);

        if (v == null)
            return "Select one vertex to start Dijkstra's from.";

        //run algorithm
        dijkstra(s, v, predecessor, edgeFromPredecessor, distance, onprogress, latch);

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

       /*// new Thread(() -> {
            dijkstra(s, v, predecessor, edgeFromPredecessor, distance, onprogress);

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
        //}).start();

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
        updateShortestPath(last, predecessor, edgeFromPredecessor);*/

        //results
        StringBuilder result = new StringBuilder();
        result.append("Vertex\tDistance\n");
        for (Vertex vertex : distance.keySet()) {
            result.append(vertex.label).append("\t").append(distance.get(vertex)).append("\n");
        }
        //add last vertex
        //result.append(last.label).append("\t").append(distance.get(last)).append("\n");
        //return result.toString();

        try {
            latch.await(); // Wait for the algorithm to complete
            LOGGER.log(Level.INFO, "in the try");
            updateShortestPath(last, predecessor, edgeFromPredecessor);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Algorithm execution was interrupted", e);
            return "Algorithm execution was interrupted.";
        }
    }
}


/*package gralog.algorithm;

import gralog.progresshandler.ProgressHandler;
import gralog.structure.Edge;
import gralog.structure.Structure;
import gralog.structure.Vertex;
import gralog.rendering.GralogColor;
import gralog.rendering.GralogGraphicsContext;
import gralog.structure.Highlights;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;
import java.util.List;



//Djikstra's algorithm description matching gralog style - adds it to list of available algorithms
@AlgorithmDescription(
    name = "Dijkstra2",
    text = "Dijkstras algorithm for shortest path",
    url = "https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm"
)

//Dijkstra class
public class Dijkstra extends Algorithm{

    private static final Logger LOGGER = Logger.getLogger(Dijkstra.class.getName());


    private ScheduledExecutorService scheduler;
    private PriorityQueue<Vertex> pQueue;
    private HashMap<Vertex, Vertex> predecessor;
    private HashMap<Vertex, Edge> edgeFromPredecessor;
    private HashMap<Vertex, Double> distance;
    private Structure<Vertex, Edge> graph;
    private ProgressHandler progress;
    private boolean directed;
    private CountDownLatch latch;
    //private List<UIUpdateListener> uiRefreshListeners = new ArrayList<>();

 // Custom method to refresh the UI
/*private void refreshUI() {
    SwingUtilities.invokeLater(() -> {
        GralogGraphicsContext gc = graph.getGraphicsContext();
        Highlights highlights = graph.getHighlights();
        for (Edge edge : graph.getEdges()) {
            edge.render(gc, highlights);
        }
        for (Vertex vertex : graph.getVertices()) {
            vertex.render(gc, highlights);
        }
        graph.repaint();
    });
}*/

  /*// Method to add UI refresh listeners
    public void addUIRefreshListener(UIRefreshListener listener) {
        uiRefreshListeners.add(listener);
    }

    // Method to notify UI refresh listeners
    private void notifyUIRefresh() {
        for (UIRefreshListener listener : uiRefreshListeners) {
            listener.onUIRefresh();
        }
    }

//step of Dijkstra's algorithm
    public void step(){
        if (!pQueue.isEmpty()){
            Vertex u = pQueue.poll();
            LOGGER.log(Level.INFO, "Processing vertex: " + u);

            // Update the UI to color the current node
            /*SwingUtilities.invokeLater(() -> {
                u.color = GralogColor.BLUE;
                //graph.repaint();
            });

            // Get all edges incident to u
            for (Edge edge : u.getIncidentEdges()) {

                ///update colour
                //edge.color = GralogColor.GREEN;
                //edge.thickness = 0.1;
                /*SwingUtilities.invokeLater(() -> {
                    edge.color = GralogColor.GREEN;
                    edge.thickness = 0.1;
                    //edge.render();
                    //graph.repaint();
                });

                // Update the UI to color the current node
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        edge.color = GralogColor.GREEN;
                        edge.thickness = 0.1;
                        //graph.repaint();
                    });
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating UI", e);
                }
                //progress.onProgress(graph);

                //try progress
                try{
                    progress.onProgress(graph);
                } catch (Exception e){
                    LOGGER.log(Level.SEVERE, "Error with progress", e);
                }

                // Check if edge is directed
                if (edge.isDirected) {
                    directed = true;
                }
                // Get the other vertex of the edge. If undirected, the target may be the same, so get the source vertex instead
                Vertex v = edge.getTarget();
                if (!directed && v.equals(u)) {
                    v = edge.getSource();
                }

                // Calculate new distance
                double dist = distance.get(u) + edge.weight;

                // If new distance is less than current distance, update distance and add to priority queue
                if (dist < distance.get(v)) {
                    distance.put(v, dist);
                    predecessor.put(v, u);
                    edgeFromPredecessor.put(v, edge);
                    pQueue.add(v);
                }

                // Reset the edge color after the delay
                /*SwingUtilities.invokeLater(() -> {
                    edge.color = GralogColor.BLACK;
                    graph.repaint();
                });
            }

            // Reset the node color after processing its edges
            /*SwingUtilities.invokeLater(() -> {
                u.color = GralogColor.BLACK;
                graph.repaint();
            });
        } else {
            LOGGER.log(Level.INFO, "Priority queue is empty, shutting down scheduler");
            scheduler.shutdown();
            latch.countDown(); // Signal that the algorithm has completed
        }
    }

    //start running
    public void runDijkstra(Structure<Vertex, Edge> graph, Vertex start, ProgressHandler progress) {
        this.graph = graph;
        this.progress = progress;
        this.predecessor = new HashMap<>();
        this.edgeFromPredecessor = new HashMap<>();
        this.distance = new HashMap<>();
        this.pQueue = new PriorityQueue<>(Comparator.comparingDouble(distance::get));
        this.directed = false;
        this.latch = new CountDownLatch(1);

        distance.put(start, 0.0);
        for (Vertex v : graph.getVertices()) {
            if (!v.equals(start)) {
                distance.put(v, Double.POSITIVE_INFINITY);
            }
        }
        pQueue.add(start);

        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Schedule the dijkstraStep method to be executed at fixed intervals
        scheduler.scheduleAtFixedRate(this::step, 0, 1, TimeUnit.SECONDS);
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

        
        runDijkstra(s, v, onprogress);

            

        /*Vertex last = null;
        double maxDistance = 0.0;
        Iterator<Vertex> vertexIterator = s.getVertices().iterator();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();
            if (distance.get(vertex) != Double.POSITIVE_INFINITY && distance.get(vertex) > maxDistance) {
                last = vertex;
                maxDistance = distance.get(vertex);
            }
        }
        //updateShortestPath(last, predecessor, edgeFromPredecessor);

        try {
            latch.await(); // Wait for the algorithm to complete
            LOGGER.log(Level.INFO, "in the try");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Algorithm execution was interrupted", e);
            return "Algorithm execution was interrupted.";
        }

        //results
        StringBuilder result = new StringBuilder();
        result.append("Vertex\tDistance\n");
        for (Vertex vertex : distance.keySet()) {
            result.append(vertex.label).append("\t").append(distance.get(vertex)).append("\n");
        }
        //add last vertex
        //result.append(last.label).append("\t").append(distance.get(last)).append("\n");
        return result.toString();
        //return null;
    }



}*/

